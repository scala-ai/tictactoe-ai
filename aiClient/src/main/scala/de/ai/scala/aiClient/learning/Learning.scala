package de.ai.scala.aiClient.learning

import de.ai.scala.aiClient.learning.action.Action
import de.ai.scala.aiClient.learning.state.State

trait Learning[S <: State, A <: Action] {
  def getDecision(state: S): A

  def trainHistory(reward: Double)
}
