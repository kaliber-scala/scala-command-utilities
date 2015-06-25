package documentation

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._

import org.qirx.littlespec.Specification
import org.qirx.littlespec.fragments.Fragment
import org.qirx.littlespec.io.Source
import org.qirx.littlespec.macros.Location

trait Documentation extends Specification {

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  def sideEffectExample[T](code: => T)(implicit asBody: T => Fragment.Body, location: Location): Fragment =
    createFragment(Source.codeAtLocation(location), { code; success })

  def await[A](f: Future[A]): A = Await.result(f, 1.second)
}