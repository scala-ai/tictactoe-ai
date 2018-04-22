package de.ai.htwg.tictactoe.aiClient.policy

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.learning.action.TicTacToeAction
import de.ai.htwg.tictactoe.aiClient.learning.action.TicTacToeActionSpace
import de.ai.htwg.tictactoe.aiClient.learning.policy.EpsGreedy
import de.ai.htwg.tictactoe.aiClient.learning.state.TicTacToeState
import de.ai.htwg.tictactoe.clientConnection.model
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import org.scalamock.scalatest.MockFactory
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class EpsGreedyTest extends FreeSpec with Matchers with MockFactory {

  val gameField: GameField = {
    val gf = model.GameField(Player.Cross, 2)
    gf.setPos(gf.posBuilder(0, 1))
  }

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

        val action = greedy.nextAction(TicTacToeState(gameField))
        action shouldBe TicTacToeAction(gameField.posBuilder(1, 1), gameField.dimensions)
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
            TicTacToeAction(gameField.posBuilder(0, 1), gameField.dimensions)
          }
        )

        val action = greedy.nextAction(TicTacToeState(gameField))
        action shouldBe TicTacToeAction(gameField.posBuilder(0, 1), gameField.dimensions)
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
            TicTacToeAction(gameField.posBuilder(0, 1), gameField.dimensions)
          }
        )

        val action = greedy.nextAction(TicTacToeState(gameField))
        action shouldBe TicTacToeAction(gameField.posBuilder(0, 1), gameField.dimensions)
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

        val action = greedy.nextAction(TicTacToeState(gameField))
        action shouldBe TicTacToeAction(gameField.posBuilder(1, 1), gameField.dimensions)
      }
    }
  }

}
