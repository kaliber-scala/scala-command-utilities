**This documentation is generated from `documentation._01_Command`**

---
The main element of this utilitity library is the `Command` trait.

The `Command` represents a computation or a set of computations. There
is a clear distinction between building up the computation and executing
it.

It provides all of the methods that allow it to use commands in for
comprehensions, allowing you to chain arbitrary computations that
potentially have different result types.

Before we can talk about the command we need to tell a bit about it's
wrapper: `Container`.

The `Container` class allows access to the `Command` trait in exchange
for a type. This type determines the types of branches that can be
present in the result.

The simplest form of command is the `DirectResult`.

```scala
import net.kaliber.commands.Container

val container = Container[BranchType]
import container._

val value = "value"
val directResult: Command[String] = DirectResult(value)

val result: Future[Either[BranchType, String]] = directResult.execute

await(result) is Right(value)
```
We have supplied aliases that might be more clear depending on your
usecase.

```scala
val instance1: Command[String] = ValueOf("x")
val instance2: Command[String] = Return("x")

def test[A](value: Command[A]): Returns[A] = value
```
A command is co-variant

```scala
val subType: Command[String] = ValueOf("")
val superType: Command[Any] = subType
```
You can use a command in a for comprehension

```scala
val command: Command[Int] =
  for {
    (one, two) <- ValueOf(1, 2)
    result <- ValueOf(one + two)
  } yield result + 1

val result = command.execute

await(result) is Right(4)
```
Note that the `map` and `flatMap` methods do not execute the function if
the result has been branched.

```scala
val branched = new Command[String] {
  def execute(implicit ec: ExecutionContext) = Future successful Left("branched")
}

val result1 = branched map (_ => sys.error("not called"))
val result2 = branched flatMap (_ => sys.error("not called"))

await(result1.execute) is Left("branched")
await(result2.execute) is Left("branched")
```
It's possible to branch a command based on its value

```scala
val initial1 = ValueOf(true)
val initial2 = ValueOf(false)

def shouldBranch(bool: Boolean): Either[Command[BranchType], Command[String]] =
  if (bool) Left(ValueOf("branched"))
  else Right(ValueOf("not branched"))

val result1 = initial1.branch(on = shouldBranch).execute
val result2 = initial2.branch(on = shouldBranch).execute

await(result1) is Left("branched")
await(result2) is Right("not branched")
```
Note that the `on` function will not be executed if the command was already
branched.

```scala
val branched = new Command[String] {
  def execute(implicit ec: ExecutionContext) = Future successful Left("branched")
}

val result = branched branch (_ => sys.error("not called"))

await(result.execute) is Left("branched")
```
