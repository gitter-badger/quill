package io.getquill

import language.dynamics
import language.experimental.macros
import scala.reflect.ClassTag
import io.getquill.ast.Ident
import io.getquill.ast.Property
import com.typesafe.config.ConfigFactory

abstract class Encoder[R: ClassTag, T: ClassTag] {
  def encode(value: T, index: Int, row: R): R
  def decode(index: Int, row: R): T
}

abstract class Source[R: ClassTag] {

  type Encoder[T] = io.getquill.Encoder[R, T]
  
  def apply[T]: Any = macro SourceMacro.entity[R, T]

  protected def config =
    ConfigFactory.load.getConfig(getClass.getSimpleName.replaceAllLiterally("$", ""))

  def run[T](q: Queryable[_, _, _]): Any = macro SourceMacro.run
}
