package de.ai.htwg.tictactoe.aiClient.learning

import scala.collection.mutable

import de.ai.htwg.tictactoe.aiClient.learning.core.net.NeuralNet
import de.ai.htwg.tictactoe.aiClient.learning.core.net.NeuralNetConfiguration
import grizzled.slf4j.Logging
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

class TTTQTable private(data: mutable.Map[Int, Double]) extends NeuralNet with Logging {
  val dimensions = 1

  override def calc(input: INDArray): INDArray = {
    val inputHash = input.hashCode()
    val value = data.get(inputHash)
    val qValue = value.fold({
      val random = 1.toDouble
      debug(s"Generate random q-value $random")
      data.put(inputHash, random)
      random
    })(p => p)
    toArray(qValue)
  }

  override def train(input: INDArray, output: INDArray): Unit = data.put(input.hashCode(), output.getDouble(0))

  override def serialize(path: String): Unit = ??? // data.map((k) => k._1 + ":" + k._2).mkString("\n")

  private def toArray(value: Double): INDArray = Nd4j.valueArrayOf(dimensions, dimensions, value)
}

object TTTQTable extends NeuralNet.Factory {
  override def apply(c: NeuralNetConfiguration): NeuralNet = new TTTQTable(mutable.Map())

  override def deserialize(string: String): NeuralNet = ???

  /*{
     val resultMap: mutable.Map[Int, Double] = mutable.Map()
     var key = ""
     var value = ""
     var s = true
     string
       .foreach {
         case ':' =>
           s = false
         case '#' =>
           resultMap.put(key.toInt, value.toDouble)
           key = ""
           value = ""
           s = true
         case a if s => key += a
         case a if !s => value += a
       }
     new TTTQTable(resultMap)
   }*/
}
