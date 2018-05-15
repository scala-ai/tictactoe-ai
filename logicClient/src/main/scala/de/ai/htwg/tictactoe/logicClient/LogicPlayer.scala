package de.ai.htwg.tictactoe.logicClient

import scala.util.Random

import akka.actor.ActorRef
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerGameFinished
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController
import grizzled.slf4j.Logging

class LogicPlayer[C <: GameFieldController](
    currentPlayer: Player,
    random: Random,
    logicPlayerActor: ActorRef,
    possibleWinActions: List[TTTWinStrategy],
) extends C#Sub with Logging {
  trace(s"LogicPlayer starts playing as $currentPlayer")
  private var todoList: List[GridPosition] = possibleWinActions(random.nextInt(possibleWinActions.size)).list
  trace(s"New game scope: $todoList")

  override def notify(pub: GameFieldController, event: GameFieldController.Updates): Unit = event match {
    case GameFieldController.Result.GameFinished(_, winner) =>
      pub.removeSubscription(this)
      logicPlayerActor ! PlayerGameFinished(winner)

    case GameFieldController.Result.GameUpdated(field) =>
      if (field.isCurrentPlayer(currentPlayer)) doGameAction(field, pub)

  }

  private def nextAction(): GridPosition = {
    val action = todoList(random.nextInt(todoList.size))
    todoList = todoList.filterNot(_ == action)
    action
  }

  private def doGameAction(field: GameField, gameController: GameFieldController): Unit = {
    trace("LogicPlayer: current turn")
    val possibleActions = field.getAllEmptyPos
    val pos = if (todoList.isEmpty) {
      possibleActions(random.nextInt(possibleActions.size))
    } else {
      val action = nextAction()
      if (possibleActions.contains(action)) {
        action
      } else {
        possibleActions(random.nextInt(possibleActions.size))
      }
    }
    gameController.setPos(pos, currentPlayer)
  }
}
