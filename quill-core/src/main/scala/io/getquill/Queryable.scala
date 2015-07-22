package io.getquill

import language.existentials
import io.getquill.ast.Query
import io.getquill.ast.QueryShow.queryShow
import io.getquill.util.Show.Shower
import language.experimental.macros

case class Queryable[R, S <: Source[R], +T](tree: Query, source: S) {

  def map[U](f: T => U): Queryable[R, S, U] = macro QueryableMacro.map[R, S, T, U]
  def flatMap[U](f: T => Queryable[R, S, U]): Queryable[R, S, U] = macro QueryableMacro.flatMap[R, S, T, U]

  def withFilter(f: T => Boolean): Queryable[R, S, T] = macro QueryableMacro.filter[R, S, T]
  def filter(f: T => Boolean): Queryable[R, S, T] = macro QueryableMacro.filter[R, S, T]

  def run: Any = macro QueryableMacro.run[T]

  override def toString = {
    import ast.QueryShow._
    tree.show
  }
}