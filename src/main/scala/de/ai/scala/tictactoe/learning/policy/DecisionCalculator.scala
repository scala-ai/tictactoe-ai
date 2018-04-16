package de.ai.scala.tictactoe.learning.policy

import de.ai.scala.tictactoe.learning.action.Action
import de.ai.scala.tictactoe.learning.action.ActionSpace
import de.ai.scala.tictactoe.learning.state.State

trait DecisionCalculator[S <: State, A <: Action] {
  def calcDecision(state: S, actionSpace: ActionSpace[S, A]): A
}
