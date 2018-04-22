package de.ai.htwg.tictactoe.aiClient.learning.state

import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player
import org.nd4j.linalg.factory.Nd4j
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class TTTStateTest extends FreeSpec with Matchers {

  "A state" - {
    "should return an n-dimensional array" - {
      "for a two-dimensional 5x5 playground" in {
        val gameField = {
          GameField(Player.Circle, 5)
            .setPos(0, 2) // circle
            .setPos(0, 0) // cross
            .setPos(1, 2) // circle
            .setPos(2, 0) // cross
            .setPos(3, 2) // circle
            .setPos(3, 0) // cross
            .setPos(4, 2) // circle
            .setPos(4, 0) // cross
            .setPos(0, 4) // circle
            .setPos(0, 1) // cross
            .setPos(1, 4) // circle
            .setPos(4, 1) // cross
            .setPos(4, 4) // circle
            .setPos(0, 3) // cross
            .setPos(3, 4) // circle
            .setPos(4, 3) // cross
            .setPos(2, 4) // circle
        }

        val state = TTTState(gameField)
        val stateAsVector = state.asVector
        stateAsVector.rank() shouldBe 2
        stateAsVector.rows() shouldBe 5
        stateAsVector.columns() shouldBe 5
        stateAsVector.getRow(0) shouldBe Nd4j.create(Array(1.0, 1.0, -1.0, 1.0, -1.0), Array(1, 5), 'c')
        stateAsVector.getRow(1) shouldBe Nd4j.create(Array(0.0, 0.0, -1.0, 0.0, -1.0), Array(1, 5), 'c')
        stateAsVector.getRow(2) shouldBe Nd4j.create(Array(1.0, 0.0, 0.0, 0.0, -1.0), Array(1, 5), 'c')
        stateAsVector.getRow(3) shouldBe Nd4j.create(Array(1.0, 0.0, -1.0, 0.0, -1.0), Array(1, 5), 'c')
        stateAsVector.getRow(4) shouldBe Nd4j.create(Array(1.0, 1.0, -1.0, 1.0, -1.0), Array(1, 5), 'c')
      }
    }
  }

}
