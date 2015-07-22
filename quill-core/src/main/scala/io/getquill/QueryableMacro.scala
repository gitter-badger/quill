package io.getquill

import scala.reflect.macros.whitebox.Context

import io.getquill.ast.Parametrized
import io.getquill.ast.ParametrizedExpr
import io.getquill.ast.Predicate
import io.getquill.ast.Query
import io.getquill.lifting.Lifting
import io.getquill.lifting.Unlifting
import io.getquill.norm.BetaReduction
import io.getquill.norm.NormalizationMacro

class QueryableMacro(val c: Context) extends Lifting with Unlifting {

  import c.universe._

  def run[T](implicit t: WeakTypeTag[T]) =
    q"${c.prefix}.source.run(${c.prefix})"

  def filter[R: WeakTypeTag, S: WeakTypeTag, T: WeakTypeTag](f: c.Expr[T => Boolean]) = {
    debug(f)
    f.tree match {
      case q"($input) => $body" if (input.name.toString.contains("ifrefutable")) =>
        c.prefix.tree
      case q"(${ alias: ast.Ident }) => ${ body: ast.Predicate }" =>
        toQueryable[R, S, T](ast.Filter(fromQueryable(c.prefix.tree), alias, body))
    }
  }

  def map[R: WeakTypeTag, S: WeakTypeTag, T: WeakTypeTag, U: WeakTypeTag](f: c.Expr[T => R]) = {
    debug(f)
    f.tree match {
      case q"(${ alias: ast.Ident }) => ${ body: ast.Expr }" =>
        toQueryable[R, S, U](ast.Map(fromQueryable(c.prefix.tree), alias, body))
    }
  }

  def flatMap[R: WeakTypeTag, S <: Source[R]: WeakTypeTag, T: WeakTypeTag, U: WeakTypeTag](f: c.Expr[T => Queryable[R, S, U]]) = {
    debug(f)
    f.tree match {
      case q"(${ alias: ast.Ident }) => ${ matchAlias: ast.Ident } match { case (..$a) => $body }" if (alias == matchAlias) =>
        val aliases =
          a.map {
            case Bind(name, _) =>
              ast.Ident(name.decodedName.toString)
          }
        val query = fromQueryable(body)
        val reduction =
          for ((a, i) <- aliases.zipWithIndex) yield {
            a -> ast.Property(alias, s"_${i + 1}")
          }
        toQueryable[R, S, U](ast.FlatMap(fromQueryable(c.prefix.tree), alias, BetaReduction(query)(reduction.toMap)))
      case q"(${ alias: ast.Ident }) => $body" =>
        toQueryable[R, S, U](ast.FlatMap(fromQueryable(c.prefix.tree), alias, fromQueryable(body)))
    }
  }

  private def toQueryable[R, S, T](query: Query)(implicit r: WeakTypeTag[R], s: WeakTypeTag[S], t: WeakTypeTag[T]) =
    q"$queryable[$r, $s, $t]($query, ${c.prefix}.source)"

  private def fromQueryable(tree: Tree) =
    tree match {
      case q"$queryable[$r, $s, $t](${ query: Query }, $source)" => query
    }

}
