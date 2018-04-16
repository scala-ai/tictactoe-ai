package de.ai.scala.tictactoe.learning.action

import de.ai.scala.tictactoe.learning.state.TicTacToeState
import de.ai.scala.tictactoe.model
import de.ai.scala.tictactoe.model.Coordinate
import de.ai.scala.tictactoe.model.Field
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class TicTacToeActionSpaceTest extends FreeSpec with Matchers {

  "An action space" - {
    "should list all empty fields in a given playground as action" in {
      val playground = model.Playground(
        Vector(
          Field.Cross, Field.Empty, Field.Circle, Field.Cross,
          Field.Cross, Field.Empty, Field.Circle, Field.Empty,
          Field.Cross, Field.Empty, Field.Empty, Field.Empty,
          Field.Cross, Field.Empty, Field.Circle, Field.Empty,
        ),
        4
      )

      val ticTacToeState = TicTacToeState(playground)
      val possibleActions = TicTacToeActionSpace().getPossibleActions(ticTacToeState)
      possibleActions.size shouldBe 8
      possibleActions should contain(TicTacToeAction(Coordinate(1, 0), playground.dimensions))
      possibleActions should contain(TicTacToeAction(Coordinate(1, 1), playground.dimensions))
      possibleActions should contain(TicTacToeAction(Coordinate(3, 1), playground.dimensions))
      possibleActions should contain(TicTacToeAction(Coordinate(1, 2), playground.dimensions))
      possibleActions should contain(TicTacToeAction(Coordinate(2, 2), playground.dimensions))
      possibleActions should contain(TicTacToeAction(Coordinate(3, 2), playground.dimensions))
      possibleActions should contain(TicTacToeAction(Coordinate(1, 3), playground.dimensions))
      possibleActions should contain(TicTacToeAction(Coordinate(3, 3), playground.dimensions))
    }
  }

}
