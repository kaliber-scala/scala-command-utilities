package net.kaliber.commands

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait ConstructorContainer[B] { _: Container[B] =>

  trait CommandConstructors {

    def apply[A, B](a: A)(implicit toCommand: A ToCommandOf B): Command[B] = toCommand(a)

    /* A ToComandOf B */
    trait ToCommandOf[-A, B] {
      def apply(a: A): Command[B]
    }

    object ToCommandOf extends LowerPriorityToCommandOf0

    trait LowerPriorityToCommandOf0 extends LowerPriorityToCommandOf1 {
      implicit def future[A] =
        new (Future[A] ToCommandOf A) {
          def apply(a: Future[A]) =
            new Command[A] {
              def execute(implicit ec: ExecutionContext): Future[Either[BranchType, A]] =
                a map (Right(_))
            }
        }
    }

    trait LowerPriorityToCommandOf1 {
      implicit def direct[A] =
        new (A ToCommandOf A) {
          def apply(a: A) = new DirectResult(a)
        }
    }
  }
}