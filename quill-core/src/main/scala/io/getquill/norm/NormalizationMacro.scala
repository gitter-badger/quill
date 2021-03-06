package io.getquill.norm

import scala.reflect.macros.whitebox.Context
import io.getquill.impl.Encoder
import io.getquill.impl.Source
import io.getquill.ast._
import io.getquill.ast.Expr
import io.getquill.ast.ExprShow.exprShow
import io.getquill.util.ImplicitResolution
import io.getquill.util.Show._
import io.getquill.lifting.Parser

trait NormalizationMacro extends ImplicitResolution with Parser {

  val c: Context
  import c.universe.{ Expr => _, Ident => _, _ }
  
  case class NormalizedQuery[R, T](query: Query, extractor: Tree)

  def normalize[D, R, T](tree: Tree)(implicit d: WeakTypeTag[D], r: WeakTypeTag[R], t: WeakTypeTag[T]) = {
    val query = Normalize(AvoidCapture(this.query(tree)))
      def inferEncoder(tpe: Type) =
        inferImplicitValueWithFallback(encoderType(c.WeakTypeTag(tpe), r).tpe, d.tpe, c.prefix.tree)
      def encoderType[T, R](implicit t: WeakTypeTag[T], r: WeakTypeTag[R]) =
        c.weakTypeTag[Encoder[R, T]]
    val (sql, materialize) = expand(inferEncoder, query)(t, r)
    NormalizedQuery(sql, materialize)
  }

  private def ensureFinalMap(query: Query): Query =
    query match {
      case FlatMap(q, x, p)    => FlatMap(q, x, ensureFinalMap(p))
      case q: Map              => query
      case q @ Filter(_, x, _) => Map(q, x, x)
      case t: Table            => Map(t, Ident("x"), Ident("x"))
    }

  private def mapExpr(query: Query): Expr =
    query match {
      case FlatMap(q, x, p) => mapExpr(p)
      case Map(q, x, p)     => p
      case other            => c.abort(c.enclosingPosition, s"Query not properly normalized, please file a bug report. $other")
    }

  private def replaceMapExpr(query: Query, expr: Expr): Query =
    query match {
      case FlatMap(q, x, p) => FlatMap(q, x, replaceMapExpr(p, expr))
      case Map(q, x, p)     => Map(q, x, expr)
      case other            => other
    }

  private def expand[T, R](inferEncoder: Type => Option[Tree], q: Query)(implicit t: WeakTypeTag[T], r: WeakTypeTag[R]) = {
    val query = ensureFinalMap(q)
    val values = expandSelect[T](inferEncoder, mapExpr(query))
    val selectColumns = selectExprs(values)
    (replaceMapExpr(query, Tuple(selectColumns.flatten)), materialize[T, R](values))
  }

  sealed trait SelectValue
  case class SimpleSelectValue(expr: Expr, encoder: Tree) extends SelectValue
  case class CaseClassSelectValue(tpe: Type, params: List[List[SimpleSelectValue]]) extends SelectValue

  private def materialize[T, R](values: List[SelectValue])(implicit t: WeakTypeTag[T], r: WeakTypeTag[R]) = {
    var index = -1
      def nextIndex = {
        index += 1
        index
      }
    val decodedValues =
      values.map {
        case SimpleSelectValue(_, encoder) =>
          q"$encoder.decode($nextIndex, row)"
        case CaseClassSelectValue(tpe, params) =>
          val decodedParams =
            params.map(_.map {
              case SimpleSelectValue(_, encoder) =>
                q"$encoder.decode($nextIndex, row)"
            })
          q"new $tpe(...$decodedParams)"
      }
    q"""
    (row: $r) => (..$decodedValues)
    """
  }

  private def selectExprs(values: List[SelectValue]) =
    values map {
      case SimpleSelectValue(expr, _)      => List(expr)
      case CaseClassSelectValue(_, params) => params.flatten.map(_.expr)
    }

  private def expandSelect[T](inferEncoder: Type => Option[Tree], mapExpr: Expr)(implicit t: WeakTypeTag[T]) = {
    val select =
      mapExpr match {
        case Tuple(values) =>
          require(values.size == t.tpe.typeArgs.size, s"Query shape doesn't match the return type $t, please file a bug report.")
          values.zip(t.tpe.typeArgs)
        case expr =>
          List(expr -> t.tpe)
      }
    select.map {
      case (expr, typ) =>
        inferEncoder(typ) match {
          case Some(encoder) =>
            SimpleSelectValue(expr, encoder)
          case None if (typ.typeSymbol.asClass.isCaseClass) =>
            val params =
              constructor(typ).paramLists.map(_.map {
                param =>
                  val paramType = param.typeSignature.typeSymbol.asType.toType
                  val encoder =
                    inferEncoder(paramType)
                      .getOrElse(c.abort(c.enclosingPosition, s"Source doesn't know how to encode '${param.name}: $paramType'"))
                  SimpleSelectValue(Property(expr, param.name.decodedName.toString), encoder)
              })
            CaseClassSelectValue(typ, params)
          case _ =>
            c.abort(c.enclosingPosition, s"Source doesn't know how to encode '${t.tpe.typeSymbol.name}.${expr.show}: $typ'")
        }
    }
  }

  private def constructor(t: Type) =
    t.members.collect {
      case m: MethodSymbol if (m.isPrimaryConstructor) => m
    }.head
}