package de.ai.htwg.tictactoe.aiClient

import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.TTTState
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController
import grizzled.slf4j.Logging


class AiPlayer[C <: GameFieldController](
    var learningUnit: TTTLearningProcessor,
    currentPlayer: Player,
    training: Boolean,
    callBack: (TTTLearningProcessor, Option[Player]) => Unit,
) extends C#Sub with Logging {
  trace(s"AiPlayer starts playing as $currentPlayer")
  def notify(pub: GameFieldController, event: GameFieldController.Updates): Unit = event match {
    case GameFieldController.Result.GameFinished(_, winner) =>

      trace("AiPlayer is finished")
      pub.removeSubscription(this)
      callBack(learningUnit, winner)

    case GameFieldController.Result.GameUpdated(field) =>
      trace("Ai received update")
      if (field.isCurrentPlayer(currentPlayer)) findGameAction(field, pub)
  }

  def findGameAction(grid: GameField, gameController: GameFieldController): Unit = {
    trace("AiPlayer: current turn")
    // if actor is in training state calculate a training decision, else calculate best action
    val (action, newLearningUnit) = if (training) {
      learningUnit.getTrainingDecision(TTTState(grid))
    } else {
      learningUnit.getBestDecision(TTTState(grid))
    }
    learningUnit = newLearningUnit
    gameController.setPos(action.coordinate, currentPlayer)
  }
}
