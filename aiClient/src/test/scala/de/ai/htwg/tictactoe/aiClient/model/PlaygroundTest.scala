package de.ai.htwg.tictactoe.aiClient.model

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class PlaygroundTest extends FreeSpec with Matchers {

  "A playground" - {
    "should return its values mapped to their coordinates" - {
      "in a 2x2 playground" in {
        val dimensions = 2
        val playground = Playground(
          Vector(
            Field.Empty, Field.Circle,
            Field.Cross, Field.Empty,
          ), dimensions
        )
        val mapToCoordinate = playground.mapToCoordinate
        println(mapToCoordinate)

        mapToCoordinate.size shouldBe 4
        mapToCoordinate should contain((GridPosition(dimensions)(0, 0), Field.Empty))
        mapToCoordinate should contain((GridPosition(dimensions)(1, 0), Field.Circle))
        mapToCoordinate should contain((GridPosition(dimensions)(0, 1), Field.Cross))
        mapToCoordinate should contain((GridPosition(dimensions)(1, 1), Field.Empty))
      }
    }
  }

}
