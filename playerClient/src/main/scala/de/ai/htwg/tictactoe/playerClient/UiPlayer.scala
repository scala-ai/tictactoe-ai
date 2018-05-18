package de.ai.htwg.tictactoe.playerClient

import de.ai.htwg.tictactoe.clientConnection.fxUI.GameUiStage
import de.ai.htwg.tictactoe.clientConnection.gameController.GameFieldController
import de.ai.htwg.tictactoe.clientConnection.gameController.GameFieldControllerSubscriber
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
      callBack: Option[Player] => Unit,
) extends GameFieldControllerSubscriber with Logging {
  trace(s"UiPlayer starts playing as $currentPlayer")
  gameUi.show()

  private def updateField(field: GameField): Unit = {
    if (field.gameField.size > gameField.gameField.size) {
      this.gameField = field
      gameUi.printField(field)
    }
  }

  override def notify(pub: GameFieldController, event: GameFieldController.Updates): Unit = event match {
    case GameFieldController.Result.GameFinished(field, winner) =>
      updateField(field)
      pub.removeSubscription(this)
      callBack(winner)

    case GameFieldController.Result.GameUpdated(field) =>
      trace(s"thread ${Thread.currentThread()}; print field: $field")
      updateField(field)
      if (field.isCurrentPlayer(currentPlayer)) doGameAction(field, pub)

  }

  private def handleMouseEvent(gameController: GameFieldController)(pos: GridPosition): Unit = {
    platform.execute {
      gameController.setPos(pos, currentPlayer)
    }
  }

  private def doGameAction(field: GameField, gameController: GameFieldController): Unit = {
    trace("UiPlayer: current turn")
    gameUi.setOnMouseClicked(handleMouseEvent(gameController))
    info("Your turn")
  }
}