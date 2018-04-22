package de.ai.htwg.tictactoe.aiClient.policy

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.learning.action.TicTacToeAction
import de.ai.htwg.tictactoe.aiClient.learning.action.TicTacToeActionSpace
import de.ai.htwg.tictactoe.aiClient.learning.policy.EpsGreedy
import de.ai.htwg.tictactoe.aiClient.learning.state.TicTacToeState
import de.ai.htwg.tictactoe.aiClient.model.Playground
import de.ai.htwg.tictactoe.aiClient.model.Field
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import org.scalamock.scalatest.MockFactory
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class EpsGreedyTest extends FreeSpec with Matchers with MockFactory {


  val dimensions = 2
  val posBuilder = GridPosition(dimensions)
  val playground = Playground(
    Vector(Field.Empty, Field.Empty, Field.Cross, Field.Empty),
    dimensions = dimensions
  )

  val actionSpace = TicTacToeActionSpace()

  "An eps greedy impl" - {
    "epoch 1 of 2 should" - {
      "return a random action if rand = 0.5" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(0.5.toFloat)
        (mockedRandom.nextInt(_: Int)).expects(3).returns(2)

        val greedy = EpsGreedy[TicTacToeState, TicTacToeAction](
          epoch = 1,
          random = mockedRandom,
          minEpsilon = 0,
          epsilonNbEpoch = 2,
          actionSpace = actionSpace,
          decisionCalculator = (_, _) => {
            fail("Action supplier should not be called!")
          }
        )

        val action = greedy.nextAction(TicTacToeState(playground))
        action shouldBe TicTacToeAction(posBuilder(1, 1), playground.dimensions)
      }
      "return a best action if rand = 0.6" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(0.6.toFloat)

        val greedy = EpsGreedy[TicTacToeState, TicTacToeAction](
          epoch = 1,
          random = mockedRandom,
          minEpsilon = 0,
          epsilonNbEpoch = 2,
          actionSpace = actionSpace,
          decisionCalculator = (_, _) => {
            TicTacToeAction(posBuilder(0, 1), playground.dimensions)
          }
        )

        val action = greedy.nextAction(TicTacToeState(playground))
        action shouldBe TicTacToeAction(posBuilder(0, 1), playground.dimensions)
      }
    }
    "epoch 2 of 2 should" - {
      "return a best action in nearly every case even if rand = 0.99999" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(0.99999.toFloat)

        val greedy = EpsGreedy[TicTacToeState, TicTacToeAction](
          epoch = 2,
          random = mockedRandom,
          minEpsilon = 0,
          epsilonNbEpoch = 2,
          actionSpace = actionSpace,
          decisionCalculator = (_, _) => {
            TicTacToeAction(posBuilder(0, 1), playground.dimensions)
          }
        )

        val action = greedy.nextAction(TicTacToeState(playground))
        action shouldBe TicTacToeAction(posBuilder(0, 1), playground.dimensions)
      }
    }
    "epoch 0 of 2 should" - {
      "return a random action in every case even if rand = 0" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(1.toFloat)
        (mockedRandom.nextInt(_: Int)).expects(3).returns(2)

        val greedy = EpsGreedy[TicTacToeState, TicTacToeAction](
          epoch = 0,
          random = mockedRandom,
          minEpsilon = 0,
          epsilonNbEpoch = 2,
          actionSpace = actionSpace,
          decisionCalculator = (_, _) => {
            fail("Action supplier should not be called!")
          }
        )

        val action = greedy.nextAction(TicTacToeState(playground))
        action shouldBe TicTacToeAction(posBuilder(1, 1), playground.dimensions)
      }
    }
  }

}
