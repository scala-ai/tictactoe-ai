package de.ai.scala.tictactoe.net

import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.nd4j.linalg.api.ndarray.INDArray

trait NeuralNet {
  def calc(input: INDArray): INDArray

  def train(input: INDArray, output: INDArray): Unit

  def persist(): Unit

  def load(): Unit

  def getConfiguration: MultiLayerConfiguration
}
