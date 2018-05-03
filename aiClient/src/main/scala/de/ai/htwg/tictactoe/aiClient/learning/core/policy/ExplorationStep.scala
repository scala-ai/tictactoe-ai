package de.ai.htwg.tictactoe.aiClient.learning.core.policy

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStep.StepSupplier
import de.ai.htwg.tictactoe.aiClient.learning.core.state.State
import grizzled.slf4j.Logging

case class ExplorationStep[S <: State, A <: Action](
    stepSupplier: StepSupplier[S],
    policyProperties: PolicyConfiguration
) extends Policy[S, A] with Logging {
  private val random = policyProperties.random
  private val minEpsilon = policyProperties.minEpsilon
  private val epsilonNbEpoch = policyProperties.epsilonNbEpochs

  override def nextAction(input: S, bestAction: () => A, possibleActions: List[A]): A = {
    val visits = stepSupplier.visitsForState(input)
    val ep = getEpsilon(visits)
    if (random.nextFloat > ep) {
      // get actual best action
      val action = bestAction()
      debug(s"calc actual best action $action (epsilon = $ep, visits = $visits)")
      action
    } else {
      // get random action
      val randomAction = possibleActions.toVector(random.nextInt(possibleActions.size))
      debug(s"use random action $randomAction (epsilon = $ep, visits = $visits)")
      randomAction
    }
  }

  override def incrementEpoch(): Policy[S, A] = copy()

  override def incrementStep(state: S): ExplorationStep[S, A] = copy(stepSupplier = stepSupplier.incrementVisit(state))

  override def resetSteps(): ExplorationStep[S, A] = copy(stepSupplier = stepSupplier.resetVisits())

  private def getEpsilon(visits: Long): Float = math.min(1f, math.max(minEpsilon, 1.toFloat - visits.toFloat / epsilonNbEpoch.toFloat))
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
    override def visitsForState(state: S): Long = map(state.hash)

    override def incrementVisit(state: S): StepSupplier[S] = copy(map.updated(state.hash, map(state.hash) + 1))

    override def resetVisits(): StepSupplier[S] = copy(Map())
  }

  object HashStepSupplier {
    def apply[S <: State](): HashStepSupplier[S] = new HashStepSupplier(Map())
  }

  def apply[S <: State, A <: Action](policyProperties: PolicyConfiguration): ExplorationStep[S, A] =
    new ExplorationStep(HashStepSupplier(), policyProperties)
}
