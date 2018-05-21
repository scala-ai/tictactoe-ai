package de.ai.htwg.tictactoe.logicClient

import scala.util.Random

import de.ai.htwg.tictactoe.clientConnection.gameController.GameController
import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerSubscriber
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player
import grizzled.slf4j.Logging

class RandomPlayer(
    currentPlayer: Player,
    random: Random,
    callbackAfterGame: Option[Player] => Unit,
) extends GameControllerSubscriber with Logging {
  trace(s"RandomPlayer starts playing as $currentPlayer")
  override def notify(pub: GameController, event: GameController.Updates): Unit = event match {
    case GameController.Result.GameFinished(_, winner) =>
      pub.removeSubscription(this)
      callbackAfterGame(winner)

    case GameController.Result.GameUpdated(field) =>
      trace("random received update")
      if (field.isCurrentPlayer(currentPlayer)) doGameAction(field, pub)
  }

  private def doGameAction(gf: GameField, gameController: GameController): Unit = {
    trace("RandomPlayer: current turn")
    val possibleActions = gf.getAllEmptyPos
    gameController.setPos(possibleActions(random.nextInt(possibleActions.size)), currentPlayer)
  }
}
