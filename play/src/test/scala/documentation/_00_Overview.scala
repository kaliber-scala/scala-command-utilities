package documentation

object _00_Overview extends Documentation {

"""|The `play` library is a small wrapper around the `core` library that is
   |focussed on improving actions in controllers.
   |
   |The example below gives you an idea of the possibilities.
   |""".stripMargin - sideEffectExample {
	   import net.kaliber.commands.play._
	   import play.api.mvc.Controller
	   import play.api.mvc.Request
	   import play.api.mvc.Result
	   import play.api.mvc.Results.InternalServerError
	   import play.api.mvc.Results.UnprocessableEntity
	   import play.api.libs.json.JsValue
	   import play.api.libs.json.Json
	   import play.api.libs.json.Json.obj
	   import scala.concurrent.Future
     import scala.util.Try

     // Declaring some types that are used in the example
     case class User(id: String, email: String)

     trait UserStore {
       def getById(id: String): Future[Option[User]]
       def save(user: User): Future[Unit]
       def list: Future[Seq[User]]
     }

     object ErrorHandling {
       implicit val json = ErrorHandler { (_, _) =>
         InternalServerError(obj("error" -> "The problem has been reported"))
       }
     }

     class UserController(store: UserStore) extends Controller {

       import ErrorHandling.json

       // Actions
       val list = Commands {
         for {
           users     <- listUsers mapWith userToJson
           usersJson <- seqToJson(users)
         } yield Ok(usersJson)
       }

       def update(id: String) = Commands { request =>
         for {
        	 _    <- getUserById(id) ifNone Return(NotFound)
           json <- jsonFromRequest(request) ifNone Return(BadRequest("invalid json"))
           user <- jsonToUser(json) ifLeft Return(validationProblemToJson)
           _    <- saveUser(user)
         } yield NoContent
       }

       // Available commands
       private def listUsers: Returns[Seq[User]] =
         Command { store.list }

       private def userToJson(user: User): Returns[JsValue] =
         Command { obj("id" -> user.id, "email" -> user.email) }

       private def seqToJson(seq: Seq[JsValue]): Returns[JsValue] =
         Command { Json toJson seq }

       private def jsonFromRequest(request: Request[Array[Byte]]): Returns[Option[JsValue]] =
         Command { Try(Json parse request.body).toOption }

       private def jsonToUser(json: JsValue): Returns[Either[String, User]] =
         Command {
           val user =
             for {
          	   id <- (json \ "id").asOpt[String]
               email <- (json \ "email").asOpt[String]
             } yield User(id, email)

           user.toRight(left = "Invalid user JSON")
         }

       private def validationProblemToJson: String => Result =
         error => UnprocessableEntity(obj("error" -> error))

       private def getUserById(id: String): Returns[Option[User]] =
         Command { store getById id }

       private def saveUser(user: User): Returns[Unit] =
         Command { store save user }
     }
   }
}