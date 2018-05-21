package de.ai.htwg.tictactoe.playerClient

import de.ai.htwg.tictactoe.clientConnection.fxUI.GameUiStage
import de.ai.htwg.tictactoe.clientConnection.gameController.GameController
import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerSubscriber
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import grizzled.slf4j.Logging

class UiPlayer(
    gameUi: GameUiStage,
    var gameField: GameField,
) extends GameControllerSubscriber with Logging {
  trace(s"UiView starts showing ui")
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
  }


}