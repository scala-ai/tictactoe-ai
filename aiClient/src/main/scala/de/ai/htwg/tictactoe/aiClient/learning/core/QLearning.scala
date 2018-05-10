package de.ai.htwg.tictactoe.aiClient.learning.core

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.core.action.ActionSpace
import de.ai.htwg.tictactoe.aiClient.learning.core.net.NeuralNet
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.Policy
import de.ai.htwg.tictactoe.aiClient.learning.core.reward.RewardCalculator
import de.ai.htwg.tictactoe.aiClient.learning.core.state.EpochResult
import de.ai.htwg.tictactoe.aiClient.learning.core.state.State
import de.ai.htwg.tictactoe.aiClient.learning.core.transition.TransitionFactory
import de.ai.htwg.tictactoe.aiClient.learning.core.transition.TransitionHistory
import grizzled.slf4j.Logging
import org.nd4j.linalg.factory.Nd4j

case class QLearning[S <: State, A <: Action, R <: EpochResult](
    policy: Policy[S, A],
    rewardCalculator: RewardCalculator[A, S, R],
    neuralNet: NeuralNet,
    transitionHistory: TransitionHistory[A, S],
    transitionFactory: TransitionFactory[A, S],
    actionSpace: ActionSpace[S, A],
    qLearningProperties: QLearningConfiguration
) extends Learning[S, A, R] with Logging {
  private val alpha = qLearningProperties.alpha
  private val gamma = qLearningProperties.gamma

  override def getTrainingDecision(state: S): (QLearning[S, A, R], A) = {
    // We are in state S
    // Let's run our Q function on S to get Q values for all possible actions
    val possibleActions = actionSpace.getPossibleActions(state)
    // action depends on a training policy
    val action = policy.nextAction(state, () => calcBestAction(state, possibleActions), possibleActions)
    createTransition(action, state)
  }

  override def getBestDecision(state: S): (QLearning[S, A, R], A) = {
    val possibleActions = actionSpace.getPossibleActions(state)
    val action = calcBestAction(state, possibleActions)
    createTransition(action, state)
  }

  private def createTransition(action: A, state: S): (QLearning[S, A, R], A) = {
    val reward = rewardCalculator.getImmediateReward(action, state)
    val updatedHistory = transitionHistory.addTransition(transitionFactory(state, action, reward))
    (copy(transitionHistory = updatedHistory), action)
  }

  private def calcBestAction(state: S, possibleActions: List[A]): A = {
    debug(s"Request action in state \n${state.asVector}")
    val ratedActions = possibleActions
      .map(a => {
        val input = Nd4j.concat(1, state.asVector, a.asVector)
        val result = neuralNet.calc(input).getDouble(0)
        debug(s"Action: $a => $result")
        assert(!result.isNaN)
        (a, result)
      })
    trace(s"Q-Values: $ratedActions")
    ratedActions.maxBy(_._2)._1
  }

  override def trainHistory(epochResult: R): QLearning[S, A, R] = {
    // last calculated q value (is the next value of actual state)
    // iterate over all transitions in history backwards
    val epochReward = rewardCalculator.getLongTermReward(epochResult)
    debug("start training (reward = " + epochReward + ")")
    transitionHistory
      .reverseTransitions()
      .foldLeft(epochReward)((futureQVal, transition) => {
        trace("train transition " + transition.action)

        val state = transition.observation
        val action = transition.action
        val reward = transition.reward

        val stateVector = state.asVector
        val actionVector = action.asVector
        // calc old q value for this action in this state
        val input = Nd4j.concat(1, stateVector, actionVector)
        val oldQVal = neuralNet.calc(input).getDouble(0)
        assert(!oldQVal.isNaN)

        // new q value Q(s, a)
        val newQVal = (1 - alpha) * oldQVal + alpha * (reward + gamma * futureQVal)
        val y = Nd4j.zeros(1)
        y.putScalar(Array[Int](0, 0), newQVal)
        neuralNet.train(input, y)
        newQVal
      })
    val updatedPolicy = transitionHistory
      .reverseTransitions()
      .foldLeft(policy)((p, s) => p.incrementStep(s.observation))
    debug("training finished")
    copy(
      policy = updatedPolicy.incrementEpoch(),
      transitionHistory = transitionHistory.truncate()
    )
  }

}
