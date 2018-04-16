package de.ai.scala.tictactoe.learning

import de.ai.scala.tictactoe.learning.action.Action
import de.ai.scala.tictactoe.learning.action.ActionSpace
import de.ai.scala.tictactoe.learning.net.NeuralNet
import de.ai.scala.tictactoe.learning.policy.DecisionCalculator
import de.ai.scala.tictactoe.learning.policy.Policy
import de.ai.scala.tictactoe.learning.reward.RewardCalculator
import de.ai.scala.tictactoe.learning.state.EpochResult
import de.ai.scala.tictactoe.learning.state.State
import de.ai.scala.tictactoe.learning.transition.TransitionFactory
import de.ai.scala.tictactoe.learning.transition.TransitionHistory
import org.nd4j.linalg.factory.Nd4j

class QLearning[S <: State, A <: Action, R <: EpochResult](
    val epochs: Int,
    val policy: Policy[S, A],
    val rewardCalculator: RewardCalculator[A, S, R],
    val actionSpace: ActionSpace[S, A],
    val neuralNet: NeuralNet,
    val transitionHistory: TransitionHistory[A, S],
    val transitionFactory: TransitionFactory[A, S],
    val decisionCalculator: DecisionCalculator[S, A]
) extends Learning[S, A] {
  override def getDecision(state: S): A = {
    // We are in state S
    // Let's run our Q function on S to get Q values for all possible actions
    val action = policy.nextAction(state)
    val reward = rewardCalculator.getImmediateReward(action, state)
    transitionHistory.addTransition(transitionFactory(state, action, reward))
    action
  }

  override def trainHistory(reward: Double): Unit = {
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
  }
}
object QLearning {
  val alpha = 0.9
  val gamma = 0.999
}
