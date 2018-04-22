package de.ai.htwg.tictactoe.aiClient.learning.core.transition

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.core.state.State

trait Transition[A <: Action, S <: State] {
  val observation: S
  val action: A
  val reward: Double
}
