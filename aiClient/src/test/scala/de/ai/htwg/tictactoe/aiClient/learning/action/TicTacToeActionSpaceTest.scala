package de.ai.htwg.tictactoe.aiClient.learning.action

import de.ai.htwg.tictactoe.aiClient.learning.state
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class TicTacToeActionSpaceTest extends FreeSpec with Matchers {

  "An action space" - {
    "should list all empty fields in a given playground as action" in {

      val gameField = {
        val builder = GridPosition(4)
        GameField(Player.Circle, 4)
          .setPos(builder(2, 0)) // circle
          .setPos(builder(0, 0)) // cross
          .setPos(builder(3, 0)) // circle
          .setPos(builder(0, 1)) // cross
          .setPos(builder(2, 1)) // circle
          .setPos(builder(0, 2)) // cross
          .setPos(builder(2, 3)) // circle
          .setPos(builder(0, 3)) // cross
      }


      val ticTacToeState = state.TicTacToeState(gameField)
      val possibleActions = TicTacToeActionSpace().getPossibleActions(ticTacToeState)
      possibleActions.size shouldBe 8
      possibleActions should contain(TicTacToeAction(gameField.posBuilder(1, 0), gameField.dimensions))
      possibleActions should contain(TicTacToeAction(gameField.posBuilder(1, 1), gameField.dimensions))
      possibleActions should contain(TicTacToeAction(gameField.posBuilder(3, 1), gameField.dimensions))
      possibleActions should contain(TicTacToeAction(gameField.posBuilder(1, 2), gameField.dimensions))
      possibleActions should contain(TicTacToeAction(gameField.posBuilder(2, 2), gameField.dimensions))
      possibleActions should contain(TicTacToeAction(gameField.posBuilder(3, 2), gameField.dimensions))
      possibleActions should contain(TicTacToeAction(gameField.posBuilder(1, 3), gameField.dimensions))
      possibleActions should contain(TicTacToeAction(gameField.posBuilder(3, 3), gameField.dimensions))
    }
  }

}
