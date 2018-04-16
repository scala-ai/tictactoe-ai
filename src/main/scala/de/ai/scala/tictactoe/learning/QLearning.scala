package de.ai.scala.tictactoe.learning

import de.ai.scala.tictactoe.learning.action.Action
import de.ai.scala.tictactoe.learning.action.ActionSpace
import de.ai.scala.tictactoe.learning.net.NeuralNet
import de.ai.scala.tictactoe.learning.policy.Policy
import de.ai.scala.tictactoe.learning.reward.RewardCalculator
import de.ai.scala.tictactoe.learning.state.EpochResult
import de.ai.scala.tictactoe.learning.state.State
import de.ai.scala.tictactoe.learning.transition.TransitionFactory
import de.ai.scala.tictactoe.learning.transition.TransitionHistory

class QLearning[S <: State, A <: Action, R <: EpochResult](
    val epochs: Int,
    val policy: Policy[S, A],
    val rewardCalculator: RewardCalculator[A, S, R],
    val actionSpace: ActionSpace[A, S],
    val neuralNet: NeuralNet,
    val transitionHistory: TransitionHistory[A, S],
    val transitionFactory: TransitionFactory[A, S]
) extends Learning[S, A] {
  override def getDecision(state: S): A = {
    // We are in state S
    // Let's run our Q function on S to get Q values for all possible actions
    val action = policy.nextAction(state)
    val reward = rewardCalculator.getImmediateReward(action, state)
    transitionHistory.addTransition(transitionFactory.create(state, action, reward))
    action
  }

  override def trainHistory(reward: Double): Unit = ???
}
