package de.ai.htwg.tictactoe.aiClient.learning.core.net

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet

trait NeuralNet {
  def calc(input: INDArray): INDArray

  def train(input: INDArray, output: INDArray): Unit

  def train(input: DataSet): Unit

  def train(inputs: List[DataSet]): Unit

  def serialize(path: String): Unit
}

object NeuralNet {

  trait Factory {
    def apply(configuration: NeuralNetConfiguration): NeuralNet

    def deserialize(string: String): NeuralNet

    def serialize(path: String, neuralNet: NeuralNet): Unit = neuralNet.serialize(path)
  }
}
