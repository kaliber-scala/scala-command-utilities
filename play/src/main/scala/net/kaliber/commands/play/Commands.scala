package net.kaliber.commands.play

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.BodyParser
import play.api.mvc.Request
import play.api.mvc.Result

import scala.concurrent.Future

object Commands {

  def apply(command: => Command[Result])(implicit errorHandler: ErrorHandler): Action[Array[Byte]] =
    Commands(_ => command)

  def apply(command: Request[Array[Byte]] => Command[Result])(implicit errorHandler: ErrorHandler): Action[Array[Byte]] =
    Commands(new ByteArrayBodyParser)(command)

  def apply[A](bodyParser: BodyParser[A])(command: Request[A] => Command[Result])(implicit errorHandler: ErrorHandler): Action[A] =
    Action.async(bodyParser) { request =>
      commandToResult(command(request)) recover errorHandler(request)
    }

  // There is no version of `apply` with the following signature:
  //   apply[A](bodyParser: BodyParser[A])(command: => Command[Result): Action[A]
  // The reason for this is that it does not make sense to parse the body and
  // then ignore it.

  private def commandToResult(command: Command[Result]): Future[Result] =
    command.execute map (_.merge)
}