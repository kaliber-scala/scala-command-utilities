package net.kaliber.commands

trait AliasContainer[B] { _: Container[B] =>
  def ValueOf[A](value: A): Command[A] = DirectResult(value)
  def Return[A](value: A): Command[A] = DirectResult(value)

  type Returns[A] = Command[A]
}