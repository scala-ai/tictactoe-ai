package de.ai.htwg.tictactoe.logicClient

import scala.util.Random

import de.ai.htwg.tictactoe.clientConnection.gameController.GameController
import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerSubscriber
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy
import grizzled.slf4j.Logging

class LogicPlayer(
    currentPlayer: Player,
    random: Random,
    possibleWinActions: List[TTTWinStrategy],
    callbackAfterGame: Option[Player] => Unit,
) extends GameControllerSubscriber with Logging {
  trace(s"LogicPlayer starts playing as $currentPlayer")
  private var todoList: List[GridPosition] = possibleWinActions(random.nextInt(possibleWinActions.size)).list
  trace(s"New game scope: $todoList")

  override def notify(pub: GameController, event: GameController.Updates): Unit = event match {
    case GameController.Result.GameFinished(_, winner) =>
      pub.removeSubscription(this)
      callbackAfterGame(winner)

    case GameController.Result.GameUpdated(field) =>
      if (field.isCurrentPlayer(currentPlayer)) doGameAction(field, pub)

  }

  private def nextAction(): GridPosition = {
    val action = todoList(random.nextInt(todoList.size))
    todoList = todoList.filterNot(_ == action)
    action
  }

  private def doGameAction(field: GameField, gameController: GameController): Unit = {
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
