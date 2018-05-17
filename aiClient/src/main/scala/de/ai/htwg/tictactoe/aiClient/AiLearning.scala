package de.ai.htwg.tictactoe.aiClient

import java.util.concurrent.Executors

import de.ai.htwg.tictactoe.aiClient.AiLearning.LearningProcessorConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.net.NeuralNetConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.PolicyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.state.EpochResult
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController

class AiLearning(properties: LearningProcessorConfiguration, trainingId: String) {
  private val aiPlayerType = Player.Cross
  private var learningUnit = TTTLearningProcessor(
    properties.dimensions,
    policyProperties = properties.policyProperties,
    qLearningProperties = properties.qLearningProperties,
    neuralNetConfiguration = properties.neuralNetProperties,
    Executors.newFixedThreadPool(5)
  )

  def saveState(): Unit = {
    learningUnit.persist(trainingId)
  }

  def registerGame(gameController: GameFieldController, training: Boolean, callbackAfterGame: Option[Player] => Unit): Unit = {
    val aiPlayer = new AiPlayer(learningUnit, aiPlayerType, training, startTrainingAfterGame(gameController.startingPlayer == aiPlayerType, callbackAfterGame))
    gameController.subscribe(aiPlayer)
  }

  def startTrainingAfterGame(isStartingPlayer: Boolean, callbackAfterGame: Option[Player] => Unit)(updatedLearningUnit: TTTLearningProcessor, winner: Option[Player]): Unit = {
    val epochResult = winner match {
      case Some(Player.Cross) => EpochResult.Won
      case Some(Player.Circle) => EpochResult.Lost
      case None => if (isStartingPlayer) EpochResult.DrawOffense else EpochResult.DrawDefense
    }

    learningUnit = updatedLearningUnit.trainResult(epochResult)
    callbackAfterGame(winner)
  }

}
object AiLearning {
  case class LearningProcessorConfiguration(
      dimensions: Int,
      policyProperties: PolicyConfiguration,
      qLearningProperties: QLearningConfiguration,
      neuralNetProperties: NeuralNetConfiguration
  )
}
