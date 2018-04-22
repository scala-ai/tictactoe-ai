package de.ai.htwg.tictactoe.aiClient.learning.transition

import de.ai.htwg.tictactoe.aiClient.learning.action.TTTAction
import de.ai.htwg.tictactoe.aiClient.learning.state.TTTState

case class TTTTransition(
    observation: TTTState,
    action: TTTAction,
    reward: Double,
) extends Transition[TTTAction, TTTState]

object TTTTransition extends TransitionFactory[TTTAction, TTTState] {
  override def apply(state: TTTState, action: TTTAction, reward: Double): TTTTransition =
    TTTTransition(state, action, reward)
}