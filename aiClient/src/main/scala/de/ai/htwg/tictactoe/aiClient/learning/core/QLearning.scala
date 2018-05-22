package de.ai.htwg.tictactoe.aiClient.learning.core

import scala.collection.mutable.ListBuffer

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
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j

case class QLearning[S <: State, A <: Action](
    policy: Policy[S, A],
    rewardCalculator: RewardCalculator[A, S],
    neuralNet: NeuralNet,
    transitionHistory: TransitionHistory[A, S],
    transitionFactory: TransitionFactory[A, S],
    actionSpace: ActionSpace[S, A],
    qLearningProperties: QLearningConfiguration,
    private val transitionBuffer: ListBuffer[DataSet] = ListBuffer()
) extends Learning[S, A] with Logging {
  private val alpha = qLearningProperties.alpha
  private val gamma = qLearningProperties.gamma

  override def getTrainingDecision(state: S): (QLearning[S, A], A) = {
    // We are in state S
    // Let's run our Q function on S to get Q values for all possible actions
    val possibleActions = actionSpace.getPossibleActions(state)
    // action depends on a training policy
    val (bestAction, bestQValue) = calcBestAction(state, possibleActions)
    val (action, qValue) = policy.nextAction(state, () => (bestAction, bestQValue), calcQValue, possibleActions)
    val reward = rewardCalculator.getImmediateReward(action, state)
    val updatedHistory = transitionHistory.addTransition(transitionFactory(state, action, reward, qValue, bestQValue))
    (copy(transitionHistory = updatedHistory), action)
  }

  override def getBestDecision(state: S): (QLearning[S, A], A) = {
    val possibleActions = actionSpace.getPossibleActions(state)
    val (action, _) = calcBestAction(state, possibleActions)
    (copy(), action)
  }

  def getXBestDecision(x: Int, state: S): (QLearning[S, A], List[A]) = {
    val possibleActions = actionSpace.getPossibleActions(state)
    val allRated = calcRatedActions(state, possibleActions).sortBy(-_._2) // minus for reverse (descending) sort
    val bestActions = allRated.drop(allRated.size - x).map(_._1)
    (copy(), bestActions)
  }

  private def calcBestAction(state: S, possibleActions: List[A]): (A, Double) = {
    calcRatedActions(state, possibleActions).maxBy(_._2)
  }

  private def calcRatedActions(state: S, possibleActions: List[A]): List[(A, Double)] = {
    debug(s"Request action in state \n${state.toString}")
    val ratedActions = possibleActions.map { a =>
      a -> calcQValue(state, a)
    }
    trace(s"Q-Values: $ratedActions")
    ratedActions
  }

  private def calcQValue(state: S, action: A): Double = {
    val input = toInputVector(state, action)
    val result = neuralNet.calc(input).getDouble(0)
    debug(s"Action: $action => $result")
    assert(!result.isNaN)
    result
  }

  override def trainHistory(epochResult: EpochResult): QLearning[S, A] = {
    // last calculated q value (is the next value of actual state)
    // iterate over all transitions in history backwards
    val epochReward = rewardCalculator.getLongTermReward(epochResult)
    debug("start training (reward = " + epochReward + ")")
    val trainingDataSet = transitionHistory
      .reverseTransitions()
      .foldLeft((epochReward, List[DataSet]()))((futureQVal, transition) => {
        trace("train transition " + transition.action)

        val state = transition.observation
        val action = transition.action
        val reward = transition.reward
        val oldQVal = transition.qValue
        val maxQVal = transition.maxQValue

        // new q value Q(s, a)
        val newQVal = (1 - alpha) * oldQVal + alpha * (reward + gamma * futureQVal._1)

        val y = Nd4j.zeros(1)
        y.putScalar(Array[Int](0, 0), newQVal)
        val input = toInputVector(state, action)
        debug(s"q value update for $action is $oldQVal -> $newQVal")
        (Math.max(newQVal, maxQVal), new DataSet(input, y) :: futureQVal._2)
      })._2
    transitionBuffer ++= trainingDataSet
    // disable batch training with batch size less than 0
    if (qLearningProperties.batchSize < 0 || transitionBuffer.size > qLearningProperties.batchSize) {
      trace(s"train ${transitionBuffer.size} elements")
      neuralNet.train(transitionBuffer.toList)
      transitionBuffer.clear()
    }
    val updatedPolicy = transitionHistory
      .reverseTransitions()
      .foldLeft(policy)((p, s) => p.incrementStep(s.observation))
    debug("training finished")
    copy(
      policy = updatedPolicy.incrementEpoch(),
      transitionHistory = transitionHistory.truncate()
    )
  }

  private def toInputVector(state: S, action: A): INDArray = {
    Nd4j.concat(1, state.asVector, action.asVector)
  }

}
