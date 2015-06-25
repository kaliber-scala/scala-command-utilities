package net.kaliber.commands

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

trait OperationsContainer[B] { _: Container[B] =>

  trait CommandOperations {

    import scala.language.implicitConversions
    implicit def extractFromCommand[A, B](command: Command[A => B]): A => Command[B] =
      a => command.map(_ apply a)

    implicit class BooleanOperations(command: Command[Boolean]) {

      def ifTrue(toResult: => Command[BranchType]): Command[Boolean] =
        command.branch(on = if (_) Left(toResult) else Right(ValueOf(false)))

      def ifFalse(toResult: => Command[BranchType]): Command[Boolean] =
        command.branch(on = if (_) Right(ValueOf(true)) else Left(toResult))
    }

    implicit class OptionOperations[A](command: Command[Option[A]]) {

      def ifNone(alternative: => Command[BranchType]): Command[A] =
        command.branch(on = _ map DirectResult[A] toRight (left = alternative))

      def ifDefined(alternative: => Command[BranchType]): Command[Unit] =
        command.branch(on = _ map (_ => alternative) toLeft (right = DirectResult(unit)))

      def ifSome(toResult: A => Command[BranchType]): Command[Unit] = {
        command.branch(on = _ map toResult toLeft (right = DirectResult(unit)))
      }

      private val unit = ()
    }

    implicit class EitherOperations[A, B](command: Command[Either[A, B]]) {

      def ifLeft(toResult: A => Command[BranchType]): Command[B] = {
        command.branch(on = _.left.map(toResult).right.map(DirectResult[B]))
      }

      def ifRight(toResult: B => Command[BranchType]): Command[A] = {
  			command.branch(on = _.right.map(toResult).left.map(DirectResult[A]).swap)
      }
    }

    implicit class SeqOperations[A](command: Command[Seq[A]]) {

      def mapWith[B](f: A => Command[B]): Command[Seq[B]] =
        command.flatMap(
          _.foldLeft(emptySeqCommand[B]) { (command, a) =>
            for {
              seqB <- command
              b    <- f(a)
            } yield seqB :+ b
          }
        )

      private def emptySeqCommand[A]: Command[Seq[A]] = DirectResult(Seq.empty)
    }
  }
}