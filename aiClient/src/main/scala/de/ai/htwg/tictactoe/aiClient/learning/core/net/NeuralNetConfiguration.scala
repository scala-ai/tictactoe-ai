package de.ai.htwg.tictactoe.aiClient.learning.core.net

import org.nd4j.linalg.activations.Activation

case class NeuralNetConfiguration(
    hiddenLayers: Int,
    hiddenNodes: Int,
    inputNodes: Int,
    activationFunction: Activation
)
object NeuralNetConfiguration {
  def apply(): NeuralNetConfiguration = new NeuralNetConfiguration(2, 16, 18, Activation.RELU)
}