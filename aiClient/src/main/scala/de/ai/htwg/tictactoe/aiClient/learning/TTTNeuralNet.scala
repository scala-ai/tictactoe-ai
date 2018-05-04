package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.core.net.NeuralNet
import grizzled.slf4j.Logging
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.rl4j.util.Constants
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions

class TTTNeuralNet() extends NeuralNet with Logging {
  private val dimensions = 4
  private val actions = dimensions * dimensions
  private val state = dimensions * dimensions
  private val inputNodes = actions + state
  private val hiddenNodes1 = 32
  private val hiddenNodes2 = 18
  private val hiddenNodes3 = 18
  private val outputNodes = 1 // q value
  // private val learningRate = 0.01

  private val configuration =
    new NeuralNetConfiguration.Builder()
      .seed(Constants.NEURAL_NET_SEED)
      //.updater(Updater.NESTEROVS).momentum(0.9)
      .updater(Adam.builder()
      .beta1(0.9)
      .beta2(0.999)
      .build())
      .biasInit(1)
      //.regularization(true).l2(1e-4)
      .l2(1e-4)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .list
      .layer(0, new DenseLayer.Builder()
        .nIn(inputNodes)
        .nOut(hiddenNodes1)
        .weightInit(WeightInit.XAVIER)
        .activation(Activation.RELU)
        .build)
      .layer(1, new DenseLayer.Builder()
        .nIn(hiddenNodes1)
        .nOut(hiddenNodes2)
        .weightInit(WeightInit.XAVIER)
        .activation(Activation.RELU)
        .build)
      .layer(2, new DenseLayer.Builder()
        .nIn(hiddenNodes2)
        .nOut(hiddenNodes3)
        .weightInit(WeightInit.XAVIER)
        .activation(Activation.RELU)
        .build)
      .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
        //.activation(Activation.SOFTMAX)
        //.activation(Activation.TANH)
        .activation(Activation.IDENTITY)
        .nIn(hiddenNodes3)
        .nOut(outputNodes)
        .weightInit(WeightInit.XAVIER)
        .build)
      .pretrain(false)
      .backprop(true)
      .build

  private val model: MultiLayerNetwork = new MultiLayerNetwork(configuration)

  model.init()

  override def calc(input: INDArray): INDArray = model.output(reshapeInput(input), true)

  override def train(input: INDArray, output: INDArray): Unit = {
    trace("train network")
    model.fit(reshapeInput(input), reshapeInput(output))
    // model.finetune()
    trace("finished train network")
  }

  override def persist(): Unit = ???

  override def load(): Unit = ???

  private def reshapeInput(input: INDArray) = input.reshape(1, input.length())
}
