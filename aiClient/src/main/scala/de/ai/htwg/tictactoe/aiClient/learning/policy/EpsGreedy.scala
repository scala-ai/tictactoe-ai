package de.ai.htwg.tictactoe.aiClient.learning.policy

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.learning.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.state.State

case class EpsGreedy[S <: State, A <: Action](
    epoch: Long,
    random: Random,
    minEpsilon: Float,
    epsilonNbEpoch: Long
) extends Policy[S, A] {

  override def nextAction(input: S, bestAction: () => A, possibleActions: List[A]): A = {
    val ep = getEpsilon
    if (random.nextFloat > ep) {
      // get actual best action
      bestAction()
    } else {
      // get random action
      possibleActions.toVector(random.nextInt(possibleActions.size))
    }
  }

  override def incrementStep(): EpsGreedy[S, A] = copy(epoch = epoch + 1)

  override def resetSteps(): EpsGreedy[S, A] = copy(epoch = 0)

  private def getEpsilon: Float = math.min(1f, math.max(minEpsilon, 1f - epoch.toFloat / epsilonNbEpoch))
}

object EpsGreedy {
  def apply[S <: State, A <: Action](
      random: Random,
      minEpsilon: Float,
      epsilonNbEpoch: Long
  ): EpsGreedy[S, A] = new EpsGreedy[S, A](0, random, minEpsilon, epsilonNbEpoch)
}
