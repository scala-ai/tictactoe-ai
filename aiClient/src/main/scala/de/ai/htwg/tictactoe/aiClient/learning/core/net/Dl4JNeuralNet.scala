package de.ai.htwg.tictactoe.aiClient.learning.core.net

import scala.collection.JavaConverters._

import grizzled.slf4j.Logging
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.deeplearning4j.nn
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions

class Dl4JNeuralNet private(val model: MultiLayerNetwork) extends NeuralNet with Logging {
  model.init()

  override def calc(input: INDArray): INDArray = model.output(reshapeInput(input), true)

  override def train(input: INDArray, output: INDArray): Unit =
    model.fit(reshapeInput(input), reshapeInput(output))

  override def train(input: DataSet): Unit =
    model.fit(reshapeDataSet(input))

  override def train(inputs: List[DataSet]): Unit =
    model.fit(new ListDataSetIterator[DataSet](inputs.map(reshapeDataSet).asJava, 5))

  private def reshapeInput(input: INDArray) = input.reshape(1, input.length())

  private def reshapeDataSet(input: DataSet) = new DataSet(reshapeInput(input.getFeatures), reshapeInput(input.getLabels))

  override def serialize(path: String): Unit =
    ModelSerializer.writeModel(model, path, true)
}

object Dl4JNeuralNet extends NeuralNet.Factory {
  private val seed = 5
  private val outputNodes = 1 // q value

  override def apply(conf: NeuralNetConfiguration): NeuralNet = {
    Nd4j.getRandom.setSeed(seed)
    var netConfig = new nn.conf.NeuralNetConfiguration.Builder()
      .seed(12345L)
      .updater(Adam.builder()
        .beta1(0.9)
        .beta2(0.999)
        .build)
      .biasInit(1)
      //.regularization(true).l2(1e-4)
      .l2(1e-4)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .list
      .layer(0, new DenseLayer.Builder()
        .nIn(conf.inputNodes)
        .nOut(conf.hiddenNodes)
        .weightInit(WeightInit.XAVIER)
        .activation(Activation.RELU)
        .build)

    1.until(conf.hiddenLayers).foreach(idx =>
      netConfig = netConfig.layer(idx, new DenseLayer.Builder()
        .nIn(conf.hiddenNodes)
        .nOut(conf.hiddenNodes)
        .weightInit(WeightInit.XAVIER)
        .activation(Activation.RELU)
        .build)
    )

    netConfig = netConfig
      .layer(conf.hiddenLayers, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
        .activation(Activation.IDENTITY)
        .nIn(conf.hiddenNodes)
        .nOut(outputNodes)
        .weightInit(WeightInit.XAVIER)
        .build)
      .pretrain(false)
      .backprop(true)

    new Dl4JNeuralNet(new MultiLayerNetwork(netConfig.build))
  }

  override def deserialize(path: String): NeuralNet = {
    Nd4j.getRandom.setSeed(seed)
    val model: MultiLayerNetwork = ModelSerializer.restoreMultiLayerNetwork(path)
    new Dl4JNeuralNet(model)
  }
}
