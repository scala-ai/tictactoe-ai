package de.ai.htwg.tictactoe.logicClient

import scala.util.Random

import akka.actor.ActorRef
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerGameFinished
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController
import grizzled.slf4j.Logging

class RandomPlayer[C <: GameFieldController](
    currentPlayer: Player,
    random: Random,
    randomPlayerActor: ActorRef,
) extends C#Sub with Logging {
  trace(s"RandomPlayer starts playing as $currentPlayer")
  override def notify(pub: GameFieldController, event: GameFieldController.Updates): Unit = event match {
    case GameFieldController.Result.GameFinished(_, winner) =>
      pub.removeSubscription(this)
      randomPlayerActor ! PlayerGameFinished(winner)

    case GameFieldController.Result.GameUpdated(field) =>
      trace("random received update")
      if (field.isCurrentPlayer(currentPlayer)) doGameAction(field, pub)
  }

  private def doGameAction(gf: GameField, gameController: GameFieldController): Unit = {
    trace("RandomPlayer: current turn")
    val possibleActions = gf.getAllEmptyPos
    gameController.setPos(possibleActions(random.nextInt(possibleActions.size)), currentPlayer)
  }
}
