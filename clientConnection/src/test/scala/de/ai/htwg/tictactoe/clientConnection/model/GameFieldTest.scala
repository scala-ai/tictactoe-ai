package de.ai.htwg.tictactoe.clientConnection.model

import org.scalatest.FreeSpec
import org.scalatest.Matchers

class GameFieldTest extends FreeSpec with Matchers {

  "A GameField" - {
    "should return its values mapped to their coordinates" - {
      "in a 2x2 GameField" in {
        val dimensions = 2
        val gameField = GameField(Player.Cross, dimensions).setPos(0, 1).setPos(1, 0)

        gameField.getAllEmptyPos().size shouldBe 2
        gameField.gameField.get(gameField.posBuilder(0, 0)) shouldBe None
        gameField.gameField.get(gameField.posBuilder(1, 0)) shouldBe Some(Player.Circle)
        gameField.gameField.get(gameField.posBuilder(0, 1)) shouldBe Some(Player.Cross)
        gameField.gameField.get(gameField.posBuilder(1, 1)) shouldBe None
      }
    }
  }


}
