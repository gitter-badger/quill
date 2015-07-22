package io.getquill

import scala.reflect.macros.whitebox.Context
import io.getquill.ast.Query
import io.getquill.lifting.Lifting
import io.getquill.lifting.Unlifting

class SourceMacro(val c: Context) extends Lifting with Unlifting {
  import c.universe._

  def entity[R, T](implicit r: WeakTypeTag[R], t: WeakTypeTag[T]): Tree = {
    val s = c.prefix.actualType
    q"$queryable[$r, $s, $t](${ast.Table(t.tpe.typeSymbol.name.toString): Query}, ${c.prefix.tree})"
  }
}
