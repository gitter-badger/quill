package io.getquill.util

import scala.reflect.macros.whitebox.Context

trait Messages {

  val c: Context

  def fail(msg: String) =
    c.abort(c.enclosingPosition, msg)
    
  def warn(msg: String) =
    c.warning(c.enclosingPosition, msg)
    
  def info(msg: String) =
    c.echo(c.enclosingPosition, msg)

}