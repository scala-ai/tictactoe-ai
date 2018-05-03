package de.ai.htwg.tictactoe.aiClient.learning.core.policy

import scala.util.Random

case class ExplorationStepConfiguration(
    minEpsilon: Float,
    nbStepVisits: Long,
    random: Random
) extends PolicyConfiguration
object ExplorationStepConfiguration {
  def apply(): ExplorationStepConfiguration = new ExplorationStepConfiguration(0.1f, 16, Random)
}


