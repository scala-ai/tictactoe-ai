package de.ai.htwg.tictactoe.aiClient.learning.core.action

import de.ai.htwg.tictactoe.aiClient.learning.TTTAction
import de.ai.htwg.tictactoe.aiClient.learning.TTTActionSpace
import de.ai.htwg.tictactoe.aiClient.learning.TTTState
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GameFieldDimensions

import org.scalatest.FreeSpec
import org.scalatest.Matchers

class TTTActionSpaceTest extends FreeSpec with Matchers {

  "An action space" - {

    val strat = new GameFieldDimensions{
      override def dimensions: Int = 4
    }

    "should list all empty fields in a given playground as action" in {

      val gameField = {
        GameField(Player.Circle, strat)
          .setPos(GridPosition(2, 0)) // circle
          .setPos(GridPosition(0, 0)) // cross
          .setPos(GridPosition(3, 0)) // circle
          .setPos(GridPosition(0, 1)) // cross
          .setPos(GridPosition(2, 1)) // circle
          .setPos(GridPosition(0, 2)) // cross
          .setPos(GridPosition(2, 3)) // circle
          .setPos(GridPosition(0, 3)) // cross
      }


      val ticTacToeState = TTTState(gameField, isStartingPlayer = true)
      val possibleActions = TTTActionSpace().getPossibleActions(ticTacToeState)
      possibleActions.size shouldBe 8
      possibleActions should contain(TTTAction(GridPosition(1, 0), gameField.dimensions))
      possibleActions should contain(TTTAction(GridPosition(1, 1), gameField.dimensions))
      possibleActions should contain(TTTAction(GridPosition(3, 1), gameField.dimensions))
      possibleActions should contain(TTTAction(GridPosition(1, 2), gameField.dimensions))
      possibleActions should contain(TTTAction(GridPosition(2, 2), gameField.dimensions))
      possibleActions should contain(TTTAction(GridPosition(3, 2), gameField.dimensions))
      possibleActions should contain(TTTAction(GridPosition(1, 3), gameField.dimensions))
      possibleActions should contain(TTTAction(GridPosition(3, 3), gameField.dimensions))
    }
  }

}
