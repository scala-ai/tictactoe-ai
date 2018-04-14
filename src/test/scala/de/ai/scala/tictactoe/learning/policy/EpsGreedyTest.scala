package de.ai.scala.tictactoe.learning.policy

import scala.util.Random

import de.ai.scala.tictactoe.learning.action.TicTacToeAction
import de.ai.scala.tictactoe.learning.action.TicTacToeActionSpace
import de.ai.scala.tictactoe.learning.state.TicTacToeState
import de.ai.scala.tictactoe.model.Coordinate
import de.ai.scala.tictactoe.model.Field
import de.ai.scala.tictactoe.model.Playground
import org.scalamock.scalatest.MockFactory
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class EpsGreedyTest extends FreeSpec with Matchers with MockFactory {

  "An eps greedy impl" - {
    "in initial state should" - {
      "return a random action" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(1)
        (mockedRandom.nextInt(_: Int)).expects(3).returns(2)
        val playground = Playground(
          Vector(Field.Empty, Field.Empty, Field.Cross, Field.Empty),
          dimensions = 2
        )
        val actionSpace = TicTacToeActionSpace()
        val greedy = EpsGreedy[TicTacToeState, TicTacToeAction](
          epoch = 0,
          random = mockedRandom,
          minEpsilon = 0,
          epsilonNbEpoch = 0,
          actionSpace = actionSpace,
          actionSupplier = (_, _) => {
            fail("Action supplier should not be called!")
          }
        )

        val action = greedy.nextAction(TicTacToeState(playground))
        action shouldBe TicTacToeAction(Coordinate(1, 1))
      }
    }
  }

}
