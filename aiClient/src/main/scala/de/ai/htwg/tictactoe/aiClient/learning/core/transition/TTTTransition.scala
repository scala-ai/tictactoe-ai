package de.ai.htwg.tictactoe.aiClient.learning.core.transition

import de.ai.htwg.tictactoe.aiClient.learning.core.action.TTTAction
import de.ai.htwg.tictactoe.aiClient.learning.core.state.TTTState

case class TTTTransition(
    observation: TTTState,
    action: TTTAction,
    reward: Double,
) extends Transition[TTTAction, TTTState]

object TTTTransition extends TransitionFactory[TTTAction, TTTState] {
  override def apply(state: TTTState, action: TTTAction, reward: Double): TTTTransition =
    new TTTTransition(state, action, reward)
}