package de.ai.scala.tictactoe.learning.action

import de.ai.scala.tictactoe.learning.state.TicTacToeState
import de.ai.scala.tictactoe.model.CircleField
import de.ai.scala.tictactoe.model.Coordinate
import de.ai.scala.tictactoe.model.CrossField
import de.ai.scala.tictactoe.model.EmptyField
import de.ai.scala.tictactoe.model.Playground
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class TicTacToeActionSpaceTest extends FreeSpec with Matchers {

  "An action space" - {
    "should list all empty fields in a given playground as action" in {
      val playground = Playground(
        Vector(
          CrossField(), EmptyField(), CircleField(), CrossField(), CircleField(),
          CrossField(), EmptyField(), CircleField(), EmptyField(), CircleField(),
          CrossField(), EmptyField(), EmptyField(), EmptyField(), CircleField(),
          CrossField(), EmptyField(), CircleField(), EmptyField(), CircleField()
        ),
        (4, 5)
      )

      val ticTacToeState = TicTacToeState(playground)
      val possibleActions = TicTacToeActionSpace().getPossibleActions(ticTacToeState)
      possibleActions.size shouldBe 8
      possibleActions should contain(TicTacToeAction(Coordinate(1, 0)))
      possibleActions should contain(TicTacToeAction(Coordinate(1, 1)))
      possibleActions should contain(TicTacToeAction(Coordinate(3, 1)))
      possibleActions should contain(TicTacToeAction(Coordinate(1, 2)))
      possibleActions should contain(TicTacToeAction(Coordinate(2, 2)))
      possibleActions should contain(TicTacToeAction(Coordinate(3, 2)))
      possibleActions should contain(TicTacToeAction(Coordinate(1, 3)))
      possibleActions should contain(TicTacToeAction(Coordinate(3, 3)))
    }
  }

}
