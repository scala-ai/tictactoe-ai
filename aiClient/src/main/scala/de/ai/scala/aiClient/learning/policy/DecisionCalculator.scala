package de.ai.scala.aiClient.learning.policy

import de.ai.scala.aiClient.learning.action.Action
import de.ai.scala.aiClient.learning.action.ActionSpace
import de.ai.scala.aiClient.learning.state.State
;

trait DecisionCalculator[S <: State, A <: Action] {
  def calcDecision(state: S, actionSpace: ActionSpace[S, A]): A
}
