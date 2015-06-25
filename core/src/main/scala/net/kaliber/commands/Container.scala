package net.kaliber.commands

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

// Note that the construction below is not meant to separate concerns. All of the
// code in the inherited types could be copy-pasted in here because they all share
// the branch type.
//
// We only chose to put them in separate files to give the reader of the code
// smaller chunks.
class Container[B]
  extends CommandContainer[B]
  with AliasContainer[B]
  with ConstructorContainer[B]
  with OperationsContainer[B] {

  protected type BranchType = B
}

object Container {
  def apply[B]: Container[B] = new Container[B]
}