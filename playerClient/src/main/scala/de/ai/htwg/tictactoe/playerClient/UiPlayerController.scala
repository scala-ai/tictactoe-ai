package de.ai.htwg.tictactoe.playerClient

import scala.concurrent.Await
import scala.concurrent.Promise
import scala.concurrent.duration.Duration

import de.ai.htwg.tictactoe.clientConnection.fxUI.GameUiStage
import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import grizzled.slf4j.Logging

class UiPlayerController(
    gameUi: GameUiStage,
    override val currentPlayer: Player,
) extends GameControllerPlayer with Logging {
  override def getMove(field: GameField): GridPosition = {
    info("Your turn")
    val promisedPos = Promise[GridPosition]()
    gameUi.setOnMouseClicked { pos =>
      promisedPos.trySuccess(pos)
    }
    val pos = Await.result(promisedPos.future, Duration.Inf)
    if (field.getPos(pos).isEmpty) {
      pos
    } else {
      getMove(field)
    }
  }
}
