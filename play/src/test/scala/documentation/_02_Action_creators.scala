package documentation

import play.twirl.api.Html
import net.kaliber.commands.play.ErrorHandler
import play.mvc.Http.RequestBuilder

object _02_Action_creators extends Documentation {

"""|In order to make working with commands easy we have provided two objects
   |that take a command and create an action from it.
   |
   |A command can be a chain of commands and somewhere along this chain
   |something can go wrong. In order to deal with the problems we need to
   |provide an error handler.
   |
   |Below an example error handler.
   |
   |Note that we have specified separate error handlers for `html` and `json`.
   |""".stripMargin - sideEffectExample {
     import net.kaliber.commands.play.ErrorHandler
     import play.api.mvc.RequestHeader
     import play.api.Logger
     import play.api.mvc.Results.InternalServerError
     import play.api.libs.json.Json.obj

     object ErrorHandling {

       implicit def json = ErrorHandler { (request, throwable) =>
         reportError(request, throwable)
         InternalServerError(obj("error" -> "An unexpected error occurred. The problem has been reported."))
       }

       implicit def html = ErrorHandler { (request, throwable) =>
         reportError(request, throwable)
         InternalServerError(views.html.error())
       }

       private def reportError(request: RequestHeader, throwable: Throwable): Unit = {
         Logger.error("Error handling a request at " + request.path, throwable)
         reportCauseOf(throwable)
       }

       private def reportCauseOf(throwable: Throwable): Unit = {
         val cause = throwable.getCause
         Option(cause).foreach { cause =>
           Logger.error("Caused by", cause)
           reportCauseOf(cause)
         }
       }

       private def reportErrorToExternalService(request: RequestHeader, throwable: Throwable): Unit = {
         // report the error to the bug tracker
       }
     }
   }

"""|In order to create an `Action` that is specialized for commands you
   |need to have an `implicit` `ErrorHandler` in scope.
   |
   |Note that the `Commands` object only accepts commands that return a
   |`Result`.
   |""".stripMargin - example {
	   import net.kaliber.commands.play._

     import play.api.mvc.{Controller, Request}
	   import play.api.mvc.Results.InternalServerError
     import play.api.test.{FakeRequest, Helpers}
     import play.api.test.Helpers._

     import scala.concurrent.Future
     import scala.language.reflectiveCalls

     object ErrorHandling {
       implicit val html = ErrorHandler { (request, throwable) =>
         InternalServerError("error")
       }
     }

     val controller =
       new Controller {
         import ErrorHandling.html

         val example = Commands { request =>
           for {
             value1 <- extractMethod(request)
             value2 <- handleMethod(value1)
           } yield Ok(value2)
         }

         private def extractMethod(request: Request[Array[Byte]]): Returns[String] =
           Command { request.method }

      	 private def handleMethod(value: String): Returns[String] =
           Command { if (value == "GET") Future successful "get called" else Future failed new RuntimeException }
       }

     val result1 = Helpers.call(controller.example, FakeRequest("GET", "/"))
     status(result1) is 200
     contentAsString(result1) is "get called"

     val result2 = Helpers.call(controller.example, FakeRequest("POST", "/"))
     status(result2) is 500
     contentAsString(result2) is "error"
   }

"""|`Commands` uses a `ByteArrayBodyParser` by default. One reason for this is
   |that the default body parsers do not allow you to customize error handling
   |other than in a global fashion.
   |
   |The other reason to use a generic `Array[Byte]` body parser is to make it
   |obvious how a request is handled and to provide some form of recovery in
   |the controller itself.
   |
   |You can specifiy another body parser as seen below. Note that the error
   |handler is used to catch the exception from the body parser.
   |""".stripMargin - example {
     import net.kaliber.commands.play._
     import net.kaliber.commands.play.ByteArrayBodyParser.MaxLengthExceededException

     import play.api.mvc.Results.BadRequest
     import play.api.mvc.Results.InternalServerError
     import play.api.mvc.Results.Ok
     import play.api.test.{FakeRequest, Helpers}
     import play.api.test.Helpers._

     implicit val html = ErrorHandler { (request, throwable) =>
       throwable match {
         case MaxLengthExceededException(message, maxLength) =>
           BadRequest(s"exceeded $maxLength")
         case _ =>
           InternalServerError("error")
       }
     }

     val example =
       Commands(bodyParser = new ByteArrayBodyParser(maxLength = 2)) { request =>
         ValueOf(Ok("ok"))
       }

     val result1 = Helpers.call(example, FakeRequest("GET", "/"))
     status(result1) is 200
     contentAsString(result1) is "ok"

     val request = FakeRequest("GET", "/").withBody(Array[Byte](1, 2, 3))
     val result2 = Helpers.call(example, request)
     status(result2) is 400
     contentAsString(result2) is "exceeded 2"
   }

"""|We also allow commands that do not require a request
   |""".stripMargin - example {
     import net.kaliber.commands.play._
     import net.kaliber.commands.play.ByteArrayBodyParser.MaxLengthExceededException

     import play.api.mvc.Results.{BadRequest, InternalServerError, Ok}
     import play.api.mvc.BodyParsers
     import play.api.test.{FakeRequest, Helpers}
     import play.api.test.Helpers._

     implicit val html = ErrorHandler { (_, _) => InternalServerError("error") }

     var counter = 0

     def updateCounter: Returns[Unit] = Command { counter += 1 }

     val example = Commands {
       for {
         _ <- updateCounter
       } yield Ok(counter.toString)
     }

     val result1 = Helpers.call(example, FakeRequest("GET", "/"))
     status(result1) is 200
     contentAsString(result1) is "1"

     val result2 = Helpers.call(example, FakeRequest("GET", "/"))
     status(result2) is 200
     contentAsString(result2) is "2"
   }

   object views {
     object html {
       def error(): Html = ???
     }
   }
}