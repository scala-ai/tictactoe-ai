package de.ai.htwg.tictactoe.aiClient.learning.policy

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.action.ActionSpace
import de.ai.htwg.tictactoe.aiClient.learning.state.State

case class EpsGreedy[S <: State, A <: Action](
    epoch: Long,
    random: Random,
    minEpsilon: Float,
    epsilonNbEpoch: Long,
    actionSpace: ActionSpace[S, A],
    decisionCalculator: DecisionCalculator[S, A]
) extends Policy[S, A] {

  override def nextAction(input: S): A = {
    val ep = getEpsilon
    if (random.nextFloat > ep) {
      // get actual best action
      decisionCalculator.calcDecision(input, actionSpace)
    } else {
      // get random action
      val possibleActions = actionSpace.getPossibleActions(input)
      possibleActions.toVector(random.nextInt(possibleActions.size))
    }
  }

  override def incrementStep(): EpsGreedy[S, A] = copy(epoch = epoch + 1)

  override def resetSteps(): EpsGreedy[S, A] = copy(epoch = 0)

  private def getEpsilon: Float = math.min(1f, math.max(minEpsilon, 1f - epoch.toFloat / epsilonNbEpoch))
}