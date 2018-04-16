package de.ai.scala.tictactoe.learning.transition

import de.ai.scala.tictactoe.learning.action.Action
import de.ai.scala.tictactoe.learning.state.State

trait Transition[A <: Action, S <: State] {
  val observation: S
  val action: A
  val reward: Double
}
