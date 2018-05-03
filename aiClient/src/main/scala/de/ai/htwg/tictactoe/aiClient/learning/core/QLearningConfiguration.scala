package de.ai.htwg.tictactoe.aiClient.learning.core

case class QLearningConfiguration(
    alpha: Double,
    gamma: Double
)

object QLearningConfiguration {
  def apply(): QLearningConfiguration = new QLearningConfiguration(0.9, 0.6)
}
