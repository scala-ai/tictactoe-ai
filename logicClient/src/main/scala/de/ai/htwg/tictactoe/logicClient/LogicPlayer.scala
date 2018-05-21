package de.ai.htwg.tictactoe.logicClient

import scala.util.Random

import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy
import grizzled.slf4j.Logging

class LogicPlayer(
    override val currentPlayer: Player,
    random: Random,
    possibleWinActions: List[TTTWinStrategy],
) extends GameControllerPlayer with Logging {
  trace(s"LogicPlayer starts playing as $currentPlayer")
  private var todoList: List[GridPosition] = possibleWinActions(random.nextInt(possibleWinActions.size)).list
  trace(s"New game scope: $todoList")


  private def nextAction(): GridPosition = {
    val action = todoList(random.nextInt(todoList.size))
    todoList = todoList.filterNot(_ == action)
    action
  }

  override def getMove(field: GameField): GridPosition = {
    trace("LogicPlayer: current turn")
    val possibleActions = field.getAllEmptyPos
    if (todoList.isEmpty) {
      possibleActions(random.nextInt(possibleActions.size))
    } else {
      val action = nextAction()
      if (possibleActions.contains(action)) {
        action
      } else {
        possibleActions(random.nextInt(possibleActions.size))
      }
    }
  }
}
