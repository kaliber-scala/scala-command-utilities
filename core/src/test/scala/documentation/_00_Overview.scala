package documentation

import scala.concurrent.Future

object _00_Overview extends Documentation {

"""|The `core` library provides a way to deal with the most common problems
   |that arise when dealing with results of different types.
   |
   |In the example below we are using functions with different return types
   |and chain them together while using the library.
   |""".stripMargin - sideEffectExample {
     import net.kaliber.commands.Container

     sealed trait ServiceProblem
     case class NoUserFoundWithId(id: String) extends ServiceProblem

     // Specify the branch type of the container
     val container = Container[ServiceProblem]

     // Import the machinery from the container
     import container._

     // Some types that we can use in the example
     case class User(id: String, email: String, name: String)
     object Store {
       def lookupById(id: String): Future[Option[User]] = ???
     }
     object Email {
       def send(recipient: String, subject: String, message: String): Future[Unit] = ???
     }

     // Here we create the different parts of our program
     def retrieveUser(id: String): Returns[Option[User]] =
       Command { Store.lookupById(id) }

     def userToRecipient(user: User): Returns[String] =
       Command { "\"${user.name}\" <${user.email}>" }

     type Subject = String
     type Message = String

     def createSubjectAndMessageFor(user: User): Returns[(Subject, Message)] =
       Command { "Important information" -> "Dear ${user.name}, ..." }

     def sendMessage(recipient: String, subject: String, message: String): Returns[Unit] =
       Command { Email.send(recipient, subject, message) }

     // The program now conveys exactly what happens without any noise from
     // type differences.
     def sendMessageForId(id: String): Returns[Unit] =
       for {
         user      <- retrieveUser(id) ifNone Return(NoUserFoundWithId(id))
         recipient <- userToRecipient(user)
         (subject,
          message) <- createSubjectAndMessageFor(user)
         _         <- sendMessage(recipient, subject, message)
       } yield ()
   }
}