package net.kaliber.commands.play


import play.api.libs.iteratee.Iteratee
import play.api.mvc.RequestHeader
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Traversable
import scala.concurrent.ExecutionContext
import play.api.libs.iteratee.Cont
import play.api.mvc.Request
import play.api.mvc.BodyParser

class ByteArrayBodyParser(maxLength: Int = 1024 * 1024 * 100)(implicit ec: ExecutionContext, errorHandler: ErrorHandler) extends BodyParser[Array[Byte]] {

  private lazy val bodyParser = getBodyParserWith(maxLength)

  def apply(requestHeader: RequestHeader) = bodyParser(requestHeader)

  private def getBodyParserWith(maxLength: Int): BodyParser[Array[Byte]] =
    BodyParser("byte array") { request =>
      Traversable.takeUpTo[Array[Byte]](maxLength)
        .transform(Iteratee.consume[Array[Byte]]())
        .flatMap(eofOrElse {
          val r = Request(request, Array.empty[Byte])
          val exception = ByteArrayBodyParser.MaxLengthExceededException("The maximum allowed length was exceeded", maxLength)
          errorHandler(r)(exception)
        })
    }

  /*
   * We duplicated the Iteratee.EofOrElse trait due to the fact that currently it's not lazy
   * This means if you do some sort of error handling within the `otherwise` it will always be executed
   * https://github.com/playframework/playframework/issues/4344
   */

  private trait EofOrElse[E] {
    def apply[A, B](otherwise: => B)(eofValue: A): Iteratee[E, Either[B, A]]
  }

  private def eofOrElse[E] = new EofOrElse[E] {
    def apply[A, B](otherwise: => B)(eofValue: A): Iteratee[E, Either[B, A]] = {
      def cont: Iteratee[E, Either[B, A]] = Cont((in: Input[E]) => {
        in match {
          case Input.El(e) => Done(Left(otherwise), in)
          case Input.EOF   => Done(Right(eofValue), in)
          case Input.Empty => cont
        }
      })
      cont
    }
  }

}
object ByteArrayBodyParser {
  case class MaxLengthExceededException(message: String, maxLength: Int) extends RuntimeException(message)
}
