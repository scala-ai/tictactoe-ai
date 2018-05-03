package de.ai.htwg.tictactoe.aiClient.learning.core.policy

import scala.util.Random

case class PolicyConfiguration(
    minEpsilon: Float,
    epsilonNbEpochs: Long,
    random: Random
)

object PolicyConfiguration {
  def apply(): PolicyConfiguration = new PolicyConfiguration(0.1f, 10000, Random)
}
