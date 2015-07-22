//package io.getquill
//
//import language.experimental.macros
//import io.getquill.ast.Query
//import io.getquill.ast.Expr
//import io.getquill.ast.ParametrizedQuery
//import io.getquill.ast.ParametrizedExpr
//import io.getquill.ast.Parametrized
//
//trait Partial {
//
//  override def toString = {
//    import util.Show._
//    import ast.QueryShow._
//    import ast.ExprShow._
//    metadata match {
//      case ParametrizedQuery(_, q) => q.show
//      case ParametrizedExpr(_, e) => e.show
//    } 
//  }
//}
//
//case class Partial1[P1, T](tree: Parametrized) extends Partial {
//
//  def apply(pr1: P1): Any = macro PartialMacro.apply1[P1, T]
//}
//
//case class Partial2[P1, P2, T](tree: Parametrized) extends Partial {
//
//  def apply(pr1: P1, pr2: P2): Any = macro PartialMacro.apply2[P1, P2, T]
//}
//
//object Partial {
//
//  def apply[P1, T](f: P1 => T): Partial1[P1, T] = macro PartialMacro.create1[P1, T]
//  def apply[P1, P2, T](f: (P1, P2) => T): Partial2[P1, P2, T] = macro PartialMacro.create2[P1, P2, T]
//}
