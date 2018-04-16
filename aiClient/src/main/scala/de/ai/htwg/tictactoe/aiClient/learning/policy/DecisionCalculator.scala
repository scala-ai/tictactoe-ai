package de.ai.htwg.tictactoe.aiClient.learning.policy

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.action.ActionSpace
import de.ai.htwg.tictactoe.aiClient.learning.state.State
;

trait DecisionCalculator[S <: State, A <: Action] {
  def calcDecision(state: S, actionSpace: ActionSpace[S, A]): A
}
