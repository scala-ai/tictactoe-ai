package de.ai.scala.aiClient.learning.action

import de.ai.scala.aiClient.learning.state.State

trait ActionSpace[S <: State, A <: Action] {
  def getPossibleActions(state: S): List[A]
}
