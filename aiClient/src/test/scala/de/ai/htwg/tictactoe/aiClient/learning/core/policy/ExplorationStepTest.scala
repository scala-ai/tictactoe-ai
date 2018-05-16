package de.ai.htwg.tictactoe.aiClient.learning.core.policy

import scala.language.higherKinds
import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.learning.TTTAction
import de.ai.htwg.tictactoe.aiClient.learning.TTTState
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStep.StepSupplier
import de.ai.htwg.tictactoe.clientConnection.model
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import org.scalamock.scalatest.MockFactory
import org.scalatest.FreeSpec
import org.scalatest.Matchers


class ExplorationStepTest extends FreeSpec with Matchers with MockFactory {

  val gameField: GameField = {
    val gf = model.GameField(Player.Cross, 2)
    gf.setPos(GridPosition(0, 1))
  }

  val possibleActions = List(
    TTTAction(GridPosition(0, 0), gameField.dimensions),
    TTTAction(GridPosition(0, 1), gameField.dimensions),
    TTTAction(GridPosition(1, 1), gameField.dimensions)
  )

  "An exploration step in" - {
    "state visited 1 of 2 should" - {
      "return a random action if rand = 0.5" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(0.5.toFloat)
        (mockedRandom.nextInt(_: Int)).expects(3).returns(2)

        val mockedStepSupplier = mock[StepSupplier[TTTState]]
        (mockedStepSupplier.visitsForState _).expects(TTTState(gameField, isStartingPlayer = true)).returns(1)

        val greedy = ExplorationStep[TTTState, TTTAction](
          mockedStepSupplier,
          ExplorationStepConfiguration(
            minEpsilon = 0,
            random = mockedRandom,
            nbStepVisits = 2
          )
        )

        val (action, qValue) = greedy.nextAction(TTTState(gameField, isStartingPlayer = true),
          () => fail("should not be called"), (_, _) => 4, possibleActions)
        action shouldBe TTTAction(GridPosition(1, 1), gameField.dimensions)
        qValue shouldBe 4
      }
      "return a best action if rand = 0.6" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(0.6.toFloat)

        val mockedStepSupplier = mock[StepSupplier[TTTState]]
        (mockedStepSupplier.visitsForState _).expects(TTTState(gameField, isStartingPlayer = true)).returns(1)

        val greedy = ExplorationStep[TTTState, TTTAction](
          mockedStepSupplier,
          ExplorationStepConfiguration(
            minEpsilon = 0,
            random = mockedRandom,
            nbStepVisits = 2
          )
        )

        val (action, qValue) = greedy.nextAction(TTTState(gameField, isStartingPlayer = true),
          () => (TTTAction(GridPosition(0, 0), gameField.dimensions), 6), (_, _) => fail("should not be called"), possibleActions)
        action shouldBe TTTAction(GridPosition(0, 0), gameField.dimensions)
        qValue shouldBe 6
      }
    }
    "epoch 2 of 2 should" - {
      "return a best action in nearly every case even if rand = 0.99999" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(0.99999.toFloat)

        val mockedStepSupplier = mock[StepSupplier[TTTState]]
        (mockedStepSupplier.visitsForState _).expects(TTTState(gameField, isStartingPlayer = true)).returns(2)

        val greedy = ExplorationStep[TTTState, TTTAction](
          mockedStepSupplier,
          ExplorationStepConfiguration(
            minEpsilon = 0,
            random = mockedRandom,
            nbStepVisits = 2
          )
        )

        val (action, qValue) = greedy.nextAction(TTTState(gameField, isStartingPlayer = true),
          () => (TTTAction(GridPosition(0, 1), gameField.dimensions), 2), (_, _) => fail("should not be called"), possibleActions)
        action shouldBe TTTAction(GridPosition(0, 1), gameField.dimensions)
        qValue shouldBe 2
      }
    }
    "epoch 0 of 2 should" - {
      "return a random action in every case even if rand = 0" in {
        val mockedRandom = mock[Random]
        (mockedRandom.nextFloat _).expects().returns(1.toFloat)
        (mockedRandom.nextInt(_: Int)).expects(3).returns(1)

        val mockedStepSupplier = mock[StepSupplier[TTTState]]
        (mockedStepSupplier.visitsForState _).expects(TTTState(gameField, isStartingPlayer = true)).returns(0)

        val greedy = ExplorationStep[TTTState, TTTAction](
          mockedStepSupplier,
          ExplorationStepConfiguration(
            minEpsilon = 0,
            random = mockedRandom,
            nbStepVisits = 2
          )
        )

        val (action, qValue) = greedy.nextAction(TTTState(gameField, isStartingPlayer = true),
          () => fail("should not be called"), (_, _) => 4, possibleActions)
        action shouldBe TTTAction(GridPosition(0, 1), gameField.dimensions)
        qValue shouldBe 4
      }
    }
  }

}
