package io.getquill.sql

import io.getquill.util.Show._
import io.getquill.norm.NormalizationMacro
import io.getquill.lifting.Unlifting
import scala.reflect.macros.whitebox.Context
import io.getquill.Queryable
import io.getquill.lifting.Lifting

class SqlSourceMacro(val c: Context)
    extends NormalizationMacro
    with Unlifting
    with Lifting {
  import c.universe._

  import SqlQueryShow._

  def run[R, S <: io.getquill.Source[R], T](q: Expr[Queryable[R, S, T]])(implicit r: WeakTypeTag[R], s: WeakTypeTag[S], t: WeakTypeTag[T]) = {
    val NormalizedQuery(query, extractor) = normalize[R, S, T](q.tree)
    val sql = SqlQuery(query).show
    c.echo(c.enclosingPosition, sql)
    q"${c.prefix}.run[$r, $s, $t]($sql, $extractor)"
  }
}
