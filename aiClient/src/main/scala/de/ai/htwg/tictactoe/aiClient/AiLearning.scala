package de.ai.htwg.tictactoe.aiClient

import java.util.concurrent.Executors

import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.net.NeuralNetConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.PolicyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.reward.RewardCalculatorConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.state.EpochResult
import de.ai.htwg.tictactoe.clientConnection.gameController.CallBackSubscriber
import de.ai.htwg.tictactoe.clientConnection.gameController.GameController
import de.ai.htwg.tictactoe.clientConnection.model.Player

class AiLearning(var learningUnit: TTTLearningProcessor, trainingId: String) {
  private val aiPlayerType = Player.Cross

  def saveState(): Unit = {
    learningUnit.persist(trainingId)
  }

  def registerGame(gameController: GameController, training: Boolean): Unit = {
    val aiPlayer = new AiPlayer(learningUnit, aiPlayerType, training)
    gameController.subscribe(CallBackSubscriber { winner: Option[Player] =>
      startTrainingAfterGame(gameController.startingPlayer == aiPlayerType)(aiPlayer.learningUnit, winner)
    })
    gameController.subscribe(aiPlayer)
  }

  def startTrainingAfterGame(isStartingPlayer: Boolean)(updatedLearningUnit: TTTLearningProcessor, winner: Option[Player]): Unit = {
    val epochResult = winner match {
      case Some(Player.Cross) => EpochResult.Won
      case Some(Player.Circle) => EpochResult.Lost
      case None => if (isStartingPlayer) EpochResult.DrawOffense else EpochResult.DrawDefense
    }

    learningUnit = updatedLearningUnit.trainResult(epochResult)
  }

}
object AiLearning {
  case class LearningProcessorConfiguration(
      dimensions: Int,
      policyProperties: PolicyConfiguration,
      qLearningProperties: QLearningConfiguration,
      neuralNetProperties: NeuralNetConfiguration,
      rewardProperties: RewardCalculatorConfiguration
  )

  def apply(properties: LearningProcessorConfiguration, trainingId: String): AiLearning =
    new AiLearning(
      TTTLearningProcessor(
        policyProperties = properties.policyProperties,
        qLearningProperties = properties.qLearningProperties,
        neuralNetConfiguration = properties.neuralNetProperties,
        Executors.newFixedThreadPool(5),
        rewardProperties = properties.rewardProperties
      ), trainingId
    )
}
