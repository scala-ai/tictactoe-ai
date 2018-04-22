package de.ai.htwg.tictactoe.aiClient.learning.action

import de.ai.htwg.tictactoe.aiClient.model
import de.ai.htwg.tictactoe.aiClient.learning.state.TicTacToeState
import de.ai.htwg.tictactoe.aiClient.model.Field
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class TicTacToeActionSpaceTest extends FreeSpec with Matchers {

  "An action space" - {
    "should list all empty fields in a given playground as action" in {
      val dimension = 4
      val posBuilder = GridPosition(dimension)
      val playground = model.Playground(
        Vector(
          Field.Cross, Field.Empty, Field.Circle, Field.Circle,
          Field.Cross, Field.Empty, Field.Circle, Field.Empty,
          Field.Cross, Field.Empty, Field.Empty, Field.Empty,
          Field.Cross, Field.Empty, Field.Circle, Field.Empty,
        ), dimension)

      val ticTacToeState = TicTacToeState(playground)
      val possibleActions = TicTacToeActionSpace().getPossibleActions(ticTacToeState)
      possibleActions.size shouldBe 8
      possibleActions should contain(TicTacToeAction(posBuilder(1, 0), playground.dimensions))
      possibleActions should contain(TicTacToeAction(posBuilder(1, 1), playground.dimensions))
      possibleActions should contain(TicTacToeAction(posBuilder(3, 1), playground.dimensions))
      possibleActions should contain(TicTacToeAction(posBuilder(1, 2), playground.dimensions))
      possibleActions should contain(TicTacToeAction(posBuilder(2, 2), playground.dimensions))
      possibleActions should contain(TicTacToeAction(posBuilder(3, 2), playground.dimensions))
      possibleActions should contain(TicTacToeAction(posBuilder(1, 3), playground.dimensions))
      possibleActions should contain(TicTacToeAction(posBuilder(3, 3), playground.dimensions))
    }
  }

}
