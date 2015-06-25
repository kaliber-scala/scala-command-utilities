package documentation

import scala.concurrent.Future

object _02_Command_construction extends Documentation {

  import net.kaliber.commands.Container

  type BranchType = Any
  val container = Container[BranchType]
  import container._

"""|We have provided a few alternative constructors for commands that might
   |come in handy. Note that this is just for your convenience, look at the
   |source code to find out how you can add more constructors. Add a pull
   |request if you want your constructor to be included in the library.
   |
   |The first one is one that simply constructs a command.
   |""".stripMargin - example {
     val value = "value"

     val result = Command(value).execute
     await(result) is Right(value)
   }

"""|The second constructor allows you to create a command based on a `Future`
   |value.
   |""".stripMargin - example {
     val value = "value"
     val future = Future successful value

     val result = Command(future).execute

     await(result) is Right(value)
   }
}