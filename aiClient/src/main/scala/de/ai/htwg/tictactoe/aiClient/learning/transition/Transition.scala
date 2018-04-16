package de.ai.htwg.tictactoe.aiClient.learning.transition

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.state.State

trait Transition[A <: Action, S <: State] {
  val observation: S
  val action: A
  val reward: Double
}
