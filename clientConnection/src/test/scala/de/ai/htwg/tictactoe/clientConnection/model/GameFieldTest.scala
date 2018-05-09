package de.ai.htwg.tictactoe.clientConnection.model

import org.scalatest.FreeSpec
import org.scalatest.Matchers

class GameFieldTest extends FreeSpec with Matchers {

  "A GameField" - {
    "should return its values mapped to their coordinates" - {
      "in a 2x2 GameField" in {
        val dimensions = 2
        val gameField = GameField(Player.Cross, dimensions).setPos(0, 1).setPos(1, 0)

        gameField.getAllEmptyPos.size shouldBe 2
        gameField.gameField.get(GridPosition(0, 0)) shouldBe None
        gameField.gameField.get(GridPosition(1, 0)) shouldBe Some(Player.Circle)
        gameField.gameField.get(GridPosition(0, 1)) shouldBe Some(Player.Cross)
        gameField.gameField.get(GridPosition(1, 1)) shouldBe None
      }
    }
    "returned hash values in a 2x2 GameField" - {
      val dimensions = 2
      val gameField1 = GameField(Player.Cross, dimensions).setPos(0, 1).setPos(1, 0)
      val gameField2 = GameField(Player.Cross, dimensions).setPos(0, 1).setPos(1, 0)
      val gameField3 = GameField(Player.Circle, dimensions).setPos(0, 1).setPos(1, 0)
      val gameField4 = GameField(Player.Circle, dimensions).setPos(1, 0).setPos(0, 1)
      val gameField5 = GameField(Player.Circle, dimensions).setPos(1, 0)
      "should be same if game fields are equal" in {
        gameField1.fieldHash shouldBe gameField2.fieldHash
        gameField2.fieldHash shouldBe gameField4.fieldHash
      }
      "should be different if game fields are not equal" in {
        gameField2.fieldHash shouldNot be(gameField3.fieldHash)
        gameField2.fieldHash shouldNot be(gameField5.fieldHash)
      }
      "should be always the same for a game field" in {
        gameField1.fieldHash shouldBe gameField1.fieldHash
        gameField2.fieldHash shouldBe gameField2.fieldHash
        gameField3.fieldHash shouldBe gameField3.fieldHash
        gameField4.fieldHash shouldBe gameField4.fieldHash
      }
    }
  }


}
