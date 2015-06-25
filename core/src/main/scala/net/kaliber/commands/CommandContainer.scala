package net.kaliber.commands

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait CommandContainer[B] { _: Container[B] =>

  trait Command[+A] {

    def map[B](f: A => B): Command[B] =
      flatMap(f andThen DirectResult[B])

    def flatMap[B](f: A => Command[B]): Command[B] =
      FlatMap(this, f)

    // Is required for pattern matching in for comprehensions, for example:
    // for { (x, y) <- tupleCommand } yield ???
    def withFilter(f: A => Boolean): Command[A] = this

    def branch[B](on: A => Either[Command[BranchType], Command[B]]): Command[B] =
      Branch(this, on)

    def execute(implicit ec: ExecutionContext): Future[Either[BranchType, A]]
  }
  object Command extends CommandConstructors with CommandOperations

  case class DirectResult[A](value: A) extends Command[A] {
    def execute(implicit ec: ExecutionContext) = Future successful Right(value)
  }

  case class FlatMap[A, B](a: Command[A], f: A => Command[B]) extends Command[B] {

    def execute(implicit ec: ExecutionContext): Future[Either[BranchType, B]] =
      a.execute.flatMap {
       case Left(result)  => Future successful Left(result)
       case Right(result) => f(result).execute
      }
  }

  case class Branch[A, B](
    command: Command[A],
    on: A => Either[Command[BranchType], Command[B]]
  ) extends Command[B] {

    def execute(implicit ec: ExecutionContext): Future[Either[BranchType, B]] =
      command.flatMap(on andThen transpose).execute flatMap {
        case Left(result) => Future successful Left(result)
        case Right(value) => Future successful value
      }

    private def transpose[A, B]: Either[Command[A], Command[B]] => Command[Either[A, B]] = {
      case Left(command)  => command.map(Left(_))
      case Right(command) => command.map(Right(_))
    }
  }
}