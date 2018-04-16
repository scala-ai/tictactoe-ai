package de.ai.htwg.tictactoe.aiClient.learning.action

import de.ai.htwg.tictactoe.aiClient.model.Coordinate
import org.nd4j.linalg.factory.Nd4j
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class TicTacToeActionTest extends FreeSpec with Matchers {

  "An action" - {
    "should return an n-dimensional array" - {
      "for a coordinate (2,3) for a 4x4 field" in {
        val dimensions = 4
        val action = TicTacToeAction(Coordinate(2, 3), dimensions)
        val actionAsVector = action.getStateAsVector
        actionAsVector.rank() shouldBe 2
        actionAsVector.rows() shouldBe dimensions
        actionAsVector.columns() shouldBe dimensions
        actionAsVector.getRow(0) shouldBe Nd4j.create(Array(.0, .0, .0, .0), Array(1, 4), 'c')
        actionAsVector.getRow(1) shouldBe Nd4j.create(Array(.0, .0, .0, .0), Array(1, 4), 'c')
        actionAsVector.getRow(2) shouldBe Nd4j.create(Array(.0, .0, .0, 1.0), Array(1, 4), 'c')
        actionAsVector.getRow(3) shouldBe Nd4j.create(Array(.0, .0, .0, .0), Array(1, 4), 'c')
      }
    }
  }

}
