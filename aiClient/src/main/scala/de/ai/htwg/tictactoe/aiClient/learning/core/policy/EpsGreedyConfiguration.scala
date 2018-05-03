package de.ai.htwg.tictactoe.aiClient.learning.core.policy

import scala.util.Random

case class EpsGreedyConfiguration(
    minEpsilon: Float,
    nbEpochVisits: Long,
    random: Random
) extends PolicyConfiguration

object EpsGreedyConfiguration {
  def apply(): EpsGreedyConfiguration = new EpsGreedyConfiguration(0.1f, 10000, Random)
}
