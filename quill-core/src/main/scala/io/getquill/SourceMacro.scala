package io.getquill

import scala.reflect.macros.whitebox.Context
import io.getquill.ast.Query
import io.getquill.lifting.Lifting
import io.getquill.lifting.Unlifting

class SourceMacro(val c: Context) extends Lifting with Unlifting {
  import c.universe._

  def run(q: Expr[Queryable[_, _, _]]) = {
    debug(q)
    q"${q.toString}"
  }

  def entity[R, T](implicit r: WeakTypeTag[R], t: WeakTypeTag[T]): Tree = {
    val s = c.prefix.actualType
    q"$queryable[$r, $s, $t](${ast.Table(t.tpe.typeSymbol.name.toString): Query}, ${c.prefix.tree})"
  }

  def selectDynamic[R](entity: Expr[String])(implicit r: WeakTypeTag[R]): Tree = {
    entity.tree match {
      case Literal(Constant(entity: String)) =>
        debug(entity)
        val s = c.prefix.actualType
        q"$queryable[$r, $s, ${TypeName(entity)}](${ast.Table(entity): Query}, ${c.prefix.tree})"
    }
  }
}
