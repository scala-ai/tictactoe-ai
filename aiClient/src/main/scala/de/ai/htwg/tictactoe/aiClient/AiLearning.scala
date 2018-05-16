package de.ai.htwg.tictactoe.aiClient

import de.ai.htwg.tictactoe.aiClient.AiLearning.LearningProcessorConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.TTTEpochResult
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.PolicyConfiguration
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController

class AiLearning(properties: LearningProcessorConfiguration, trainingId: String) {
  private var learningUnit = TTTLearningProcessor(
    properties.dimensions,
    policyProperties = properties.policyProperties,
    qLearningProperties = properties.qLearningProperties,
  )

  def saveState(): Unit = {
    learningUnit.persist(trainingId)
  }

  def registerGame(gameController: GameFieldController, training: Boolean, callbackAfterGame: Option[Player] => Unit): Unit = {
    val aiPlayer = new AiPlayer(learningUnit, Player.Cross, training, startTrainingAfterGame(callbackAfterGame))
    gameController.subscribe(aiPlayer)
  }

  def startTrainingAfterGame(callbackAfterGame: Option[Player] => Unit)(updatedLearningUnit: TTTLearningProcessor, winner: Option[Player]): Unit = {
    val epochResult = winner match {
      case None => TTTEpochResult.undecided
      case Some(Player.Cross) => TTTEpochResult.won
      case Some(Player.Circle) => TTTEpochResult.lost
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
  )
}
