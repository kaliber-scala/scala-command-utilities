package net.kaliber.commands.play

import net.kaliber.commands.Container
import play.api.mvc.Result

trait Aliases {
  val container = Container[Result]

  type Command[+A] = container.Command[A]
  val Command = container.Command

  def ValueOf[A](value:A): Command[A] = container.ValueOf(value)
  def Return[A](value:A): Command[A] = container.Return(value)

  type Returns[A] = container.Returns[A]
}