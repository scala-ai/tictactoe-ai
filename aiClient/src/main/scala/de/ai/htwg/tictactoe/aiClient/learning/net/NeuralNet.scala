package de.ai.htwg.tictactoe.aiClient.learning.net

import org.nd4j.linalg.api.ndarray.INDArray

trait NeuralNet {
  def calc(input: INDArray): INDArray

  def train(input: INDArray, output: INDArray): Unit

  def persist(): Unit

  def load(): Unit
}
