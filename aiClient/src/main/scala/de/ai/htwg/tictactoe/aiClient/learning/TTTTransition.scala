package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.core.transition.Transition
import de.ai.htwg.tictactoe.aiClient.learning.core.transition.TransitionFactory

case class TTTTransition(
    observation: TTTState,
    action: TTTAction,
    reward: Double,
    qValue: Double
) extends Transition[TTTAction, TTTState]

object TTTTransition extends TransitionFactory[TTTAction, TTTState] {
  override def apply(state: TTTState, action: TTTAction, reward: Double, qValue: Double): TTTTransition =
    new TTTTransition(state, action, reward, qValue)
}