package io.getquill.lifting

import scala.reflect.macros.whitebox.Context
import io.getquill.ast._
import io.getquill.Queryable

trait Lifting {
  val c: Context
  import c.universe.{ Function => _, Expr => _, Ident => _, Constant => _, _ }

  private val ast = q"io.getquill.ast"

  implicit val queryLift: Liftable[Query] = Liftable[Query] {
    case Table(name) =>
      q"$ast.Table($name)"
    case Filter(query, alias, body) =>
      q"$ast.Filter($query, $alias, $body)"
    case Map(query, alias, body) =>
      q"$ast.Map($query, $alias, $body)"
    case FlatMap(query, alias, body) =>
      q"$ast.FlatMap($query, $alias, $body)"
  }

  implicit val exprLift: Liftable[Expr] = Liftable[Expr] {
    case Subtract(a, b) =>
      q"$ast.Subtract($a, $b)"
    case Add(a, b) =>
      q"$ast.Add($a, $b)"
    case predicate: Predicate =>
      q"$predicate"
    case ref: Ref =>
      q"$ref"
  }

  implicit val predicateLift: Liftable[Predicate] = Liftable[Predicate] {
    case Equals(a, b) =>
      q"$ast.Equals($a, $b)"
    case And(a, b) =>
      q"$ast.And($a, $b)"
    case GreaterThan(a, b) =>
      q"$ast.GreaterThan($a, $b)"
    case GreaterThanOrEqual(a, b) =>
      q"$ast.GreaterThanOrEqual($a, $b)"
    case LessThan(a, b) =>
      q"$ast.LessThan($a, $b)"
    case LessThanOrEqual(a, b) =>
      q"$ast.LessThanOrEqual($a, $b)"
  }

  implicit val refLift: Liftable[Ref] = Liftable[Ref] {
    case Property(ref, name) =>
      q"$ast.Property($ref, $name)"
    case Ident(ident) =>
      q"$ast.Ident($ident)"
    case v: Value =>
      q"$v"
  }

  implicit val valueLift: Liftable[Value] = Liftable[Value] {
    case Constant(v) =>
      q"$ast.Constant(${Literal(c.universe.Constant(v))})"
    case NullValue =>
      q"$ast.NullValue"
    case Tuple(values) =>
      q"$ast.Tuple(List(..$values))"
  }

  implicit val identLift: Liftable[Ident] = Liftable[Ident] {
    case Ident(name) =>
      q"$ast.Ident($name)"
  }

  implicit val parametrizedLift: Liftable[Parametrized] = Liftable[Parametrized] {
    case ParametrizedQuery(params, query) =>
      q"$ast.ParametrizedQuery(List(..$params), $query)"
    case ParametrizedExpr(params, expr) =>
      q"$ast.ParametrizedExpr(List(..$params), $expr)"
  }
}