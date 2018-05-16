package de.ai.htwg.tictactoe.aiClient.learning.core.policy

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStep.StepSupplier
import de.ai.htwg.tictactoe.aiClient.learning.core.state.State
import grizzled.slf4j.Logging

case class ExplorationStep[S <: State, A <: Action](
    stepSupplier: StepSupplier[S],
    policyProperties: ExplorationStepConfiguration
) extends Policy[S, A] with Logging {
  private val random = policyProperties.random
  private val minEpsilon = policyProperties.minEpsilon
  private val nbStepVisits = policyProperties.nbStepVisits

  override def nextAction(input: S, bestAction: () => (A, Double), qValueSupplier: (S, A) => Double, possibleActions: List[A]): (A, Double) = {
    val visits = stepSupplier.visitsForState(input)
    val ep = getEpsilon(visits)
    if (random.nextFloat > ep) {
      // get actual best action
      val (action, qValue) = bestAction()
      debug(s"calc actual best action $action (qValue = $qValue, epsilon = $ep, visits = $visits)")
      (action, qValue)
    } else {
      // get random action
      val randomAction = possibleActions.toVector(random.nextInt(possibleActions.size))
      val qValue = qValueSupplier(input, randomAction)
      debug(s"use random action $randomAction (qValue = $qValue, epsilon = $ep, visits = $visits)")
      (randomAction, qValue)
    }
  }

  override def incrementEpoch(): Policy[S, A] = copy()

  override def incrementStep(state: S): ExplorationStep[S, A] = copy(stepSupplier = stepSupplier.incrementVisit(state))

  override def resetSteps(): ExplorationStep[S, A] = copy(stepSupplier = stepSupplier.resetVisits())

  private def getEpsilon(visits: Long): Float = math.min(1f, math.max(minEpsilon, 1.toFloat - visits.toFloat / nbStepVisits.toFloat))
}

object ExplorationStep {
  trait StepSupplier[S <: State] {
    def visitsForState(state: S): Long

    def incrementVisit(state: S): StepSupplier[S]

    def resetVisits(): StepSupplier[S]
  }

  case class HashStepSupplier[S <: State](
      map: Map[Int, Long]
  ) extends StepSupplier[S] {
    override def visitsForState(state: S): Long = get(state)

    override def incrementVisit(state: S): StepSupplier[S] = copy(map.updated(state.hash, get(state) + 1))

    override def resetVisits(): StepSupplier[S] = copy(Map())

    private def get(state: State) = if (map.contains(state.hash)) map(state.hash) else 0L
  }

  object HashStepSupplier {
    def apply[S <: State](): HashStepSupplier[S] = new HashStepSupplier(Map())
  }

  def apply[S <: State, A <: Action](policyProperties: ExplorationStepConfiguration): ExplorationStep[S, A] =
    new ExplorationStep(HashStepSupplier(), policyProperties)
}
