package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.action.ActionSpace
import de.ai.htwg.tictactoe.aiClient.learning.net.NeuralNet
import de.ai.htwg.tictactoe.aiClient.learning.policy.DecisionCalculator
import de.ai.htwg.tictactoe.aiClient.learning.policy.Policy
import de.ai.htwg.tictactoe.aiClient.learning.reward.RewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.state.EpochResult
import de.ai.htwg.tictactoe.aiClient.learning.state.State
import de.ai.htwg.tictactoe.aiClient.learning.transition.TransitionFactory
import de.ai.htwg.tictactoe.aiClient.learning.transition.TransitionHistory
import org.nd4j.linalg.factory.Nd4j

case class QLearning[S <: State, A <: Action, R <: EpochResult](
    policy: Policy[S, A],
    rewardCalculator: RewardCalculator[A, S, R],
    actionSpace: ActionSpace[S, A],
    neuralNet: NeuralNet,
    transitionHistory: TransitionHistory[A, S],
    transitionFactory: TransitionFactory[A, S],
    decisionCalculator: DecisionCalculator[S, A]
) extends Learning[S, A] {
  override def getDecision(state: S): (QLearning[S, A, R], A) = {
    // We are in state S
    // Let's run our Q function on S to get Q values for all possible actions
    val action = policy.nextAction(state)
    val reward = rewardCalculator.getImmediateReward(action, state)
    val updatedHistory = transitionHistory.addTransition(transitionFactory(state, action, reward))
    (copy(transitionHistory = updatedHistory), action)
  }

  override def trainHistory(reward: Double): QLearning[S, A, R] = {
    // last calculated q value (is the next value of actual state)
    // iterate over all transitions in history backwards
    transitionHistory
      .reverseTransitions()
      .foldLeft(reward)((futureQVal, transition) => {
        val state = transition.observation
        val action = transition.action
        val reward = transition.reward

        // calc old q value for this action in this state
        val input = Nd4j.concat(1, state.getStateAsVector, action.getStateAsVector)
        val oldQVal = neuralNet.calc(input).getDouble(0)

        // new q value Q(s, a)
        val newQVal = (1 - QLearning.alpha) * oldQVal + QLearning.alpha * (reward + QLearning.gamma * futureQVal)
        val y = Nd4j.zeros(1)
        y.putScalar(Array[Int](0, 0), newQVal)
        neuralNet.train(input, y)
        newQVal
      })
    copy(
      policy = policy.incrementStep(),
      transitionHistory = transitionHistory.truncate()
    )
  }
}
object QLearning {
  val alpha = 0.9
  val gamma = 0.999
}
