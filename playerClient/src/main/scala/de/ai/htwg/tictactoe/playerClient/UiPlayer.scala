package de.ai.htwg.tictactoe.playerClient

import akka.actor.ActorRef
import de.ai.htwg.tictactoe.clientConnection.fxUI.GameUiStage
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerGameFinished
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController
import grizzled.slf4j.Logging

class UiPlayer[C <: GameFieldController](
    currentPlayer: Player,
    playerUiActor: ActorRef,
    gameUi: GameUiStage,
    var gameField: GameField,
) extends C#Sub with Logging {
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
      playerUiActor ! PlayerGameFinished(winner)

    case GameFieldController.Result.GameUpdated(field) =>
      trace(s"thread ${Thread.currentThread()}; print field: $field")
      updateField(field)
      if (field.isCurrentPlayer(currentPlayer)) doGameAction(field, pub)

  }

  private def handleMouseEvent(gameController: GameFieldController)(pos: GridPosition): Unit = {
    gameController.setPosThreadSave(pos, currentPlayer)
  }

  private def doGameAction(field: GameField, gameController: GameFieldController): Unit = {
    trace("UiPlayer: current turn")
    gameUi.setOnMouseClicked(handleMouseEvent(gameController))
    info("Your turn")
  }
}