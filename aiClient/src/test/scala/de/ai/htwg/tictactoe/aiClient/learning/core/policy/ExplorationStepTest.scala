package de.ai.htwg.tictactoe.aiClient.learning.core.policy

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.learning.TTTAction
import de.ai.htwg.tictactoe.aiClient.learning.TTTState
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStep.StepSupplier
import de.ai.htwg.tictactoe.clientConnection.model
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player
import org.scalamock.scalatest.MockFactory
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class ExplorationStepTest extends FreeSpec with Matchers with MockFactory {

  val gameField: GameField = {
    val gf = model.GameField(Player.Cross, 2)
    gf.setPos(gf.posBuilder(0, 1))
  }

  val possibleActions = List(
    TTTAction(gameField.posBuilder(0, 0), gameField.dimensions),
    TTTAction(gameField.posBuilder(0, 1), gameField.dimensions),
    TTTAction(gameField.posBuilder(1, 1), gameField.dimensions)
  )

  "An exploration step in" - {
    "state visited 1 of 2 should" - {
      "return a random action if rand = 0.5" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(0.5.toFloat)
        (mockedRandom.nextInt(_: Int)).expects(3).returns(2)

        val mockedStepSupplier = mock[StepSupplier[TTTState]]
        (mockedStepSupplier.visitsForState _).expects(TTTState(gameField)).returns(1)

        val greedy = ExplorationStep[TTTState, TTTAction](
          mockedStepSupplier,
          ExplorationStepConfiguration(
            minEpsilon = 0,
            random = mockedRandom,
            nbStepVisits = 2
          )
        )

        val action = greedy.nextAction(TTTState(gameField), () => fail("should not be called"), possibleActions)
        action shouldBe TTTAction(gameField.posBuilder(1, 1), gameField.dimensions)
      }
      "return a best action if rand = 0.6" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(0.6.toFloat)

        val mockedStepSupplier = mock[StepSupplier[TTTState]]
        (mockedStepSupplier.visitsForState _).expects(TTTState(gameField)).returns(1)

        val greedy = ExplorationStep[TTTState, TTTAction](
          mockedStepSupplier,
          ExplorationStepConfiguration(
            minEpsilon = 0,
            random = mockedRandom,
            nbStepVisits = 2
          )
        )

        val action = greedy.nextAction(TTTState(gameField),
          () => TTTAction(gameField.posBuilder(0, 0), gameField.dimensions), possibleActions)
        action shouldBe TTTAction(gameField.posBuilder(0, 0), gameField.dimensions)
      }
    }
    "epoch 2 of 2 should" - {
      "return a best action in nearly every case even if rand = 0.99999" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(0.99999.toFloat)

        val mockedStepSupplier = mock[StepSupplier[TTTState]]
        (mockedStepSupplier.visitsForState _).expects(TTTState(gameField)).returns(2)

        val greedy = ExplorationStep[TTTState, TTTAction](
          mockedStepSupplier,
          ExplorationStepConfiguration(
            minEpsilon = 0,
            random = mockedRandom,
            nbStepVisits = 2
          )
        )

        val action = greedy.nextAction(TTTState(gameField),
          () => TTTAction(gameField.posBuilder(0, 1), gameField.dimensions), possibleActions)
        action shouldBe TTTAction(gameField.posBuilder(0, 1), gameField.dimensions)
      }
    }
    "epoch 0 of 2 should" - {
      "return a random action in every case even if rand = 0" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(1.toFloat)
        (mockedRandom.nextInt(_: Int)).expects(3).returns(1)

        val mockedStepSupplier = mock[StepSupplier[TTTState]]
        (mockedStepSupplier.visitsForState _).expects(TTTState(gameField)).returns(0)

        val greedy = ExplorationStep[TTTState, TTTAction](
          mockedStepSupplier,
          ExplorationStepConfiguration(
            minEpsilon = 0,
            random = mockedRandom,
            nbStepVisits = 2
          )
        )

        val action = greedy.nextAction(TTTState(gameField), () => fail("should not be called"), possibleActions)
        action shouldBe TTTAction(gameField.posBuilder(0, 1), gameField.dimensions)
      }
    }
  }

}
