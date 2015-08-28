#Scala command utilities

This library provides utilities that help create more readable code. It does this by providing an abstraction that makes dealing with types easier.

```scala
def update(id: String) = Commands { request =>
  for {
    _    <- getUserById(id) ifNone Return(NotFound)
    json <- jsonFromRequest(request) ifNone Return(BadRequest("invalid json"))
    user <- jsonToUser(json) ifLeft Return(validationProblemToJson)
    _    <- saveUser(user)
  } yield NoContent
}

```

Instead of

```scala
def update(id: String) = Action.async { request =>

  getUserById(id)
    .map(_.toRight(left = NotFound))
    .map {
      case Right(_)    => jsonFromRequest(request).toRight(left = BadRequest("invalid json"))
      case Left(other) => Left(other)
    }
    .map {
      case Right(json) => jsonToUser(json).left.map(validationProblemToJson)
      case Left(other) => Left(other)
    }
    .flatMap {
      case Right(user) => saveUser(user).map(Right(_))
      case Left(other) => Future successful Left(other)
    }
    .map(_.right.map(_ => NoContent).merge)
}
```



In our development work we found we often have to wire together functions that return a variety of types that are most of the time not directly compatible. For example:

- `Future[A]`
- `Future[Option[A]]`
- `A`
- `Option[A]`
- `Future[Either[A, B]]`

This library makes working with results like these easier. The intent of this library is to be able to use different return types without extra effort. It would allow you for example to mix plain results with `Future` values.

This library consists of two parts:

1. `core` The main machinery
2. `play` A specialized version for the Play Framework


##Installation

``` scala
libraryDependencies += "net.kaliber" %% "scala-command-utilities-core" % "0.2"
// if you are using play use the following (you can skip the core as it's automatically loaded)
libraryDependencies += "net.kaliber" %% "scala-command-utilities-play" % "0.2"

resolvers += "Rhinofly Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local"
```

##Usage

Please refer to the documentation directories in the `core` and `play` directories.
