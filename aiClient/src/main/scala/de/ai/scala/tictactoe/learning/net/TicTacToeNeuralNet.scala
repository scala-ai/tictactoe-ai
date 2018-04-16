package de.ai.scala.tictactoe.learning.net

import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.rl4j.util.Constants
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions

class TicTacToeNeuralNet extends NeuralNet {
  private val dimensions = 3
  private val actions = dimensions * dimensions
  private val state = dimensions * dimensions
  private val inputNodes = actions + state
  private val hiddenNodes1 = 18
  private val hiddenNodes2 = 18
  private val hiddenNodes3 = 18
  private val outputNodes = 1 // q value
  private val learningRate = 0.01

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
      .regularization(true)
      .l2(1e-4)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .learningRate(learningRate)
      .iterations(1).list.layer(0, new DenseLayer.Builder()
      .nIn(inputNodes)
      .nOut(hiddenNodes1)
      //.weightInit(WeightInit.XAVIER)
      .activation(Activation.RELU)
      .build
    ).layer(1, new DenseLayer.Builder()
      .nIn(hiddenNodes1)
      .nOut(hiddenNodes2)
      .activation(Activation.RELU)
      .build
    ).layer(2, new DenseLayer.Builder()
      .nIn(hiddenNodes2)
      .nOut(hiddenNodes3)
      .activation(Activation.RELU)
      .build
    ).layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
      //.activation(Activation.SOFTMAX)
      //.activation(Activation.TANH)
      .activation(Activation.IDENTITY)
      .nIn(hiddenNodes3)
      .nOut(outputNodes)
      .build
    ).pretrain(false)
      .backprop(true)
      .build

  private val model: MultiLayerNetwork = new MultiLayerNetwork(configuration)

  model.init()

  override def calc(input: INDArray): INDArray = model.output(input, true)

  override def train(input: INDArray, output: INDArray): Unit = {
    model.fit(input, output)
    model.finetune()
  }

  override def persist(): Unit = ???

  override def load(): Unit = ???
}
