package de.ai.scala.tictactoe.learning.state

import de.ai.scala.tictactoe.model.CircleField
import de.ai.scala.tictactoe.model.CrossField
import de.ai.scala.tictactoe.model.EmptyField
import de.ai.scala.tictactoe.model.Playground
import org.nd4j.linalg.factory.Nd4j
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class TicTacToeStateTest extends FreeSpec with Matchers {

  "A state" - {
    "should return an n-dimensional array" - {
      "for a two-dimensional 5x5 playground" in {
        val playground = Playground(
          Vector(
            CrossField(), EmptyField(), CircleField(), CrossField(), CircleField(),
            CrossField(), EmptyField(), CircleField(), EmptyField(), CircleField(),
            CrossField(), EmptyField(), EmptyField(), EmptyField(), CircleField(),
            CrossField(), EmptyField(), CircleField(), EmptyField(), CircleField(),
            CrossField(), CrossField(), CircleField(), CrossField(), CircleField()
          ),
          (5, 5)
        )
        val state = TicTacToeState(playground)
        val stateAsVector = state.getStateAsVector
        stateAsVector.rank() shouldBe 2
        stateAsVector.rows() shouldBe 5
        stateAsVector.columns() shouldBe 5
        stateAsVector.getRow(0) shouldBe Nd4j.create(Array(1.0, 0.0, -1.0, 1.0, -1.0), Array(1, 5), 'c')
        stateAsVector.getRow(1) shouldBe Nd4j.create(Array(1.0, 0.0, -1.0, 0.0, -1.0), Array(1, 5), 'c')
        stateAsVector.getRow(2) shouldBe Nd4j.create(Array(1.0, 0.0, 0.0, 0.0, -1.0), Array(1, 5), 'c')
        stateAsVector.getRow(3) shouldBe Nd4j.create(Array(1.0, 0.0, -1.0, 0.0, -1.0), Array(1, 5), 'c')
        stateAsVector.getRow(4) shouldBe Nd4j.create(Array(1.0, 1.0, -1.0, 1.0, -1.0), Array(1, 5), 'c')
      }
    }
  }

}