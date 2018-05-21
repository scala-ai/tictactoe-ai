package de.ai.htwg.tictactoe.playerClient

import de.ai.htwg.tictactoe.clientConnection.fxUI.GameUiStage
import de.ai.htwg.tictactoe.clientConnection.gameController.GameController
import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerSubscriber
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.util.SingleThreadPlatform
import grizzled.slf4j.Logging

class UiPlayer(
    currentPlayer: Player,
    gameUi: GameUiStage,
    var gameField: GameField,
    platform: SingleThreadPlatform,
) extends GameControllerSubscriber with Logging {
  trace(s"UiPlayer starts playing as $currentPlayer")
  gameUi.show()

  private def updateField(field: GameField): Unit = {
    if (field.gameField.size > gameField.gameField.size) {
      this.gameField = field
      gameUi.printField(field)
    }
  }

  override def notify(pub: GameController, event: GameController.Updates): Unit = event match {
    case GameController.Result.GameFinished(field, _) =>
      updateField(field)
      pub.removeSubscription(this)

    case GameController.Result.GameUpdated(field) =>
      trace(s"thread ${Thread.currentThread()}; print field: $field")
      updateField(field)
      if (field.isCurrentPlayer(currentPlayer)) doGameAction(field, pub)

  }

  private def handleMouseEvent(gameController: GameController)(pos: GridPosition): Unit = {
    platform.execute {
      gameController.setPos(pos, currentPlayer)
    }
  }

  private def doGameAction(field: GameField, gameController: GameController): Unit = {
    trace("UiPlayer: current turn")
    gameUi.setOnMouseClicked(handleMouseEvent(gameController))
    info("Your turn")
  }
}