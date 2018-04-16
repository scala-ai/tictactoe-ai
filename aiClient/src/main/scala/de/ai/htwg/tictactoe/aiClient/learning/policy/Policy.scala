package de.ai.htwg.tictactoe.aiClient.learning.policy

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.action.ActionSpace
import de.ai.htwg.tictactoe.aiClient.learning.state.State

trait Policy[S <: State, A <: Action] {
  def nextAction(state: S): A

  def incrementStep(): Policy[S, A]

  def resetSteps(): Policy[S, A]
}

trait ActionSupplier[S <: State, A <: Action] {
  def get(state: S, actionSpace: ActionSpace[S, A]): A
}
