package de.ai.htwg.tictactoe.aiClient.learning.core.policy

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.core.state.State
import grizzled.slf4j.Logging

case class EpsGreedy[S <: State, A <: Action](
    epoch: Long,
    policyProperties: EpsGreedyConfiguration
) extends Policy[S, A] with Logging {
  private val random = policyProperties.random
  private val minEpsilon = policyProperties.minEpsilon
  private val epsilonNbEpoch = policyProperties.nbEpochVisits

  override def nextAction(input: S, bestAction: () => (A, Double), qValueSupplier: (S, A) => Double, possibleActions: List[A]): (A, Double) = {
    val ep = getEpsilon
    if (random.nextFloat > ep) {
      // get actual best action
      val (action, qValue) = bestAction()
      debug(s"calc actual best action $action (qValue = $qValue, epsilon = $ep)")
      (action, qValue)
    } else {
      // get random action
      val randomAction = possibleActions.toVector(random.nextInt(possibleActions.size))
      val qValue = qValueSupplier(input, randomAction)
      debug(s"use random action $randomAction (qValue = $qValue, epsilon = $ep)")
      (randomAction, qValue)
    }
  }

  override def incrementEpoch(): Policy[S, A] = copy(epoch = epoch + 1)

  override def incrementStep(state: S): EpsGreedy[S, A] = copy()

  override def resetSteps(): EpsGreedy[S, A] = copy(epoch = 0)

  private def getEpsilon: Float = math.min(1f, math.max(minEpsilon, 1f - epoch.toFloat / epsilonNbEpoch))
}

object EpsGreedy {
  def apply[S <: State, A <: Action](policyProperties: EpsGreedyConfiguration): EpsGreedy[S, A] =
    new EpsGreedy[S, A](0, policyProperties)
}
