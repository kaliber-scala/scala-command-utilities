package documentation

import scala.concurrent.Future

object _03_Command_operations extends Documentation {

  import net.kaliber.commands.Container
  type BranchType = Any
  val container = Container[BranchType]

  import container._

"""|Some return types are typically used later on as a decision point (think
   |`Boolean` or `Option`).
   |
   |So support common cases we have enhanced the `Command` trait with extra
   |methods.
   |
   |The first ones operate on `Boolean` types.
   |""".stripMargin - example {
     def test(value1: Boolean, value2: Boolean) =
       for {
         _ <- ValueOf(value1) ifTrue Return("value1 true")
         _ <- ValueOf(value2) ifFalse Return("value2 false")
       } yield "value"

     val result1 = await(test(true, false).execute)
     val result2 = await(test(false, false).execute)
     val result3 = await(test(false, true).execute)

     result1 is Left("value1 true")
     result2 is Left("value2 false")
     result3 is Right("value")
   }

  "Note that the above methods do not execute the command if they do not branch\n" - {
    var executed = false
    def failureCommand = Command(Future successful (executed = true))

    def test(value1: Boolean, value2: Boolean) =
      for {
        _ <- ValueOf(value1) ifTrue failureCommand
        _ <- ValueOf(value2) ifFalse failureCommand
      } yield "ok"

     await(test(false, true).execute)

     executed is false
  }

"""|The second set of operations helps in dealing with `Option` types.
   |""".stripMargin - example {

     def toValue: String => String = _ + "!"

     def test(value1: Option[String], value2: Option[String], value3: Option[String]) =
       for {
         value1 <- ValueOf(value1) ifNone Return("value1 empty")
         _      <- ValueOf(value2) ifDefined Return("value2 non-empty")
         _      <- ValueOf(value3) ifSome Return(toValue)
       } yield value1

     val result1 = await(test(None, None, None).execute)
     val result2 = await(test(Some("value1"), Some("value2"), None).execute)
     val result3 = await(test(Some("value1"), None, Some("value3")).execute)
     val result4 = await(test(Some("value1"), None, None).execute)

     result1 is Left("value1 empty")
     result2 is Left("value2 non-empty")
     result3 is Left("value3!")
     result4 is Right("value1")
   }

  "Note that the above methods do not execute the command if they do not branch\n" - {
    var executed = false
    def failureCommand = Command(Future successful (executed = true))

    def test(value1: Option[String], value2: Option[String]) =
      for {
        _ <- ValueOf(value1) ifNone failureCommand
        _ <- ValueOf(value2) ifDefined failureCommand
      } yield "ok"

     await(test(Some("value"), None).execute)

     executed is false
  }

"""|The third set of operations is for `Either` instances.
   |""".stripMargin - example {
     def test(value1: Either[String, String], value2: Either[String, String]) =
       for {
         value1 <- ValueOf(value1) ifLeft ValueOf((_: String) + " is left")
         value2 <- ValueOf(value2) ifRight ValueOf((_: String) + " is right")
       } yield value1 + "-" + value2

     val result1 = await(test(Left("value1"), Left("value2")).execute)
     val result2 = await(test(Right("value1"), Right("value2")).execute)
     val result3 = await(test(Right("value1"), Left("value2")).execute)

     result1 is Left("value1 is left")
     result2 is Left("value2 is right")
     result3 is Right("value1-value2")
   }

"""|In order to give you more freedom we have provided a method that lifts
   |functions resulting in a command into a command.
   |""".stripMargin - example {
     def toResult: Command[String => String] = ValueOf(value => value + "!")

     val command = ValueOf(Some("test")) ifSome toResult

     val r = await(command.execute)
     r is Left("test!")
   }

"""|The next set op operations is for `Seq` instances.
   |""".stripMargin - {
     val result =
       for {
         mapped <- ValueOf(Seq(1, 2, 3)) mapWith (value => ValueOf(value + 1))
       } yield mapped

     await(result.execute) is Right(Seq(2, 3, 4))
   }
}