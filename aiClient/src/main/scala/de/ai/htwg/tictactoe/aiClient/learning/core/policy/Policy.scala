package de.ai.htwg.tictactoe.aiClient.learning.core.policy

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.core.state.State

trait Policy[S <: State, A <: Action] {
  def nextAction(state: S, bestAction: () => A, possibleActions: List[A]): A

  def incrementStep(): Policy[S, A]

  def resetSteps(): Policy[S, A]
}