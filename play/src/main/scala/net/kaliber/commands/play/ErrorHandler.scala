package net.kaliber.commands.play

import play.api.mvc.Result
import play.api.mvc.RequestHeader

class ErrorHandler(f: (RequestHeader, Throwable) => Result) {

  def apply(request: RequestHeader): Throwable => Result =
    f(request, _)
}

object ErrorHandler {
  def apply(f: (RequestHeader, Throwable) => Result): ErrorHandler =
    new ErrorHandler(f)
}