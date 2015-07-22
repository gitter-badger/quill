package io.getquill.lifting

import scala.reflect.macros.whitebox.Context
import io.getquill.Queryable
import io.getquill.ast._
import io.getquill.Queryable

trait Unlifting {
  val c: Context
  import c.universe.{ Function => _, Expr => _, Ident => _, Constant => _, _ }

  private val ast = q"io.getquill.ast"
  val queryable = q"io.getquill.Queryable"
  
  implicit val queryUnlift: Unliftable[Query] = Unliftable[Query] {
    case q"$ast.Table.apply(${ name: String })" =>
      Table(name)
    case q"$ast.Filter.apply(${ source: Query }, ${ alias: Ident }, ${ body: Predicate })" =>
      Filter(source, alias, body)
    case q"$ast.Map.apply(${ source: Query }, ${ alias: Ident }, ${ body: Expr })" =>
      Map(source, alias, body)
    case q"$ast.FlatMap.apply(${ source: Query }, ${ alias: Ident }, ${ body: Query })" =>
      FlatMap(source, alias, body)
  }

  implicit val exprUnlift: Unliftable[Expr] = Unliftable[Expr] {
    case q"$ast.Subtract.apply(${ a: Expr }, ${ b: Expr })" =>
      Subtract(a, b)
    case q"$ast.Add.apply(${ a: Expr }, ${ b: Expr })" =>
      Add(a, b)
    case q"${ a: Expr } - ${ b: Expr }" =>
      Subtract(a, b)
    case q"${ a: Expr } + ${ b: Expr }" =>
      Add(a, b)
    case q"${ predicate: Predicate }" =>
      predicate
    case q"${ ref: Ref }" =>
      ref
  }

  implicit val predicateUnlift: Unliftable[Predicate] = Unliftable[Predicate] {
    case q"$ast.Equals.apply(${ a: Expr }, ${ b: Expr })" =>
      Equals(a, b)
    case q"${ a: Expr } == ${ b: Expr }" =>
      Equals(a, b)

    case q"$ast.And.apply(${ a: Predicate }, ${ b: Predicate })" =>
      And(a, b)
    case q"${ a: Predicate } && ${ b: Predicate }" =>
      And(a, b)

    case q"$ast.GreaterThanOrEqual.apply(${ a: Expr }, ${ b: Expr })" =>
      GreaterThanOrEqual(a, b)
    case q"${ a: Expr } >= ${ b: Expr }" =>
      GreaterThanOrEqual(a, b)

    case q"$ast.GreaterThan.apply(${ a: Expr }, ${ b: Expr })" =>
      GreaterThan(a, b)
    case q"${ a: Expr } > ${ b: Expr }" =>
      GreaterThan(a, b)

    case q"$ast.LessThanOrEqual.apply(${ a: Expr }, ${ b: Expr })" =>
      LessThanOrEqual(a, b)
    case q"${ a: Expr } <= ${ b: Expr }" =>
      LessThanOrEqual(a, b)

    case q"$ast.LessThan.apply(${ a: Expr }, ${ b: Expr })" =>
      LessThan(a, b)
    case q"${ a: Expr } < ${ b: Expr }" =>
      LessThan(a, b)
  }

  implicit val refUnlift: Unliftable[Ref] = Unliftable[Ref] {
    case q"$ast.Property.apply(${ expr: Expr }, ${ name: String })" =>
      Property(expr, name)
    case q"${ value: Value }" =>
      value
    case q"${ ident: Ident }" =>
      ident
    case q"${ expr: Expr }.$property" =>
      Property(expr, property.decodedName.toString)
  }

  implicit val valueUnlift: Unliftable[Value] = Unliftable[Value] {
    case q"$ast.Tuple.apply(immutable.this.List.apply[$t](..$v))" =>
      val values =
        v.map {
          case q"${ expr: Expr }" => expr
        }
      Tuple(values)
    case q"$ast.Constant.apply(${ Literal(c.universe.Constant(v)) })" =>
      Constant(v)
    case q"$ast.NullValue" =>
      NullValue
    case q"null" =>
      NullValue
    case Literal(c.universe.Constant(v)) =>
      Constant(v)
    case q"((..$v))" if (v.size > 1) =>
      val values =
        v.map {
          case q"${ expr: Expr }" => expr
        }
      Tuple(values)
  }

  implicit val identUnift: Unliftable[Ident] = Unliftable[Ident] {
    case q"$ast.Ident.apply(${ name: String })" =>
      Ident(name)
    case t: ValDef =>
      Ident(t.name.decodedName.toString)
    case c.universe.Ident(TermName(name)) =>
      Ident(name)
    case q"${ name: Ident }: $typ" =>
      name
    case c.universe.Bind(TermName(name), c.universe.Ident(termNames.WILDCARD)) =>
      Ident(name)
  }

  implicit val parametrizedUnlift: Unliftable[Parametrized] = Unliftable[Parametrized] {
    case q"${ p: ParametrizedQuery }" => p
    case q"${ p: ParametrizedExpr }"  => p
  }

  implicit val parametrizedQueryUnlift: Unliftable[ParametrizedQuery] = Unliftable[ParametrizedQuery] {
    case q"$ast.ParametrizedQuery.apply(immutable.this.List.apply[$t](..$p), ${ query: Query })" =>
      val params =
        p.map {
          case q"${ ident: Ident }" => ident
        }
      ParametrizedQuery(params, query)
  }

  implicit val parametrizedExprUnlift: Unliftable[ParametrizedExpr] = Unliftable[ParametrizedExpr] {
    case q"$ast.ParametrizedExpr.apply(immutable.this.List.apply[$t](..$p), ${ expr: Expr })" =>
      val params =
        p.map {
          case q"${ ident: Ident }" => ident
        }
      ParametrizedExpr(params, expr)
  }

  def debug[T](v: T) = {
    c.info(c.enclosingPosition, v.toString(), false)
    v
  }
}
