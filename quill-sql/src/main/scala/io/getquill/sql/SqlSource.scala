package io.getquill.sql

import scala.reflect.ClassTag
import language.experimental.macros
import io.getquill.Queryable

abstract class SqlSource[R: ClassTag] extends io.getquill.Source[R] {

  override def run[T](q: Queryable[R, this.type, T]): Any = macro SqlSourceMacro.run[R, this.type, T]

  def run[T](sql: String, extractor: R => T): List[T]
}
