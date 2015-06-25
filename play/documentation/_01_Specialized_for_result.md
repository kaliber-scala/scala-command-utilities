**This documentation is generated from `documentation._01_Specialized_for_result`**

---
In play the `Result` type plays an important role. Actions in controllers
solely deal with mapping a `Request` to a `Result`. This gives `Result` a
special status in the context of this library.

That's why we have provided aliases for commands and their aliases prefilled
with `Result` as their branch type.

```scala
import net.kaliber.commands.play.{Command, ValueOf, Return, Returns}

val value = "test"
val command = Command(value)

// Note the branch type is set to result
val result: Future[Either[Result, String]] = command.execute

await(result) is Right(value)
```
You can access the specialized container directly if you wish.

```scala
import net.kaliber.commands.play.container._
```
