package de.ai.scala.aiClient.model

import org.scalatest.FreeSpec
import org.scalatest.Matchers

class PlaygroundTest extends FreeSpec with Matchers {

  "A playground" - {
    "should return its values mapped to their coordinates" - {
      "in a 2x2 playground" in {
        val playground = Playground(
          Vector(
            Field.Empty, Field.Circle,
            Field.Cross, Field.Empty,
          ), 2
        )
        val mapToCoordinate = playground.mapToCoordinate
        println(mapToCoordinate)

        mapToCoordinate.size shouldBe 4
        mapToCoordinate should contain((Coordinate(0, 0), Field.Empty))
        mapToCoordinate should contain((Coordinate(1, 0), Field.Circle))
        mapToCoordinate should contain((Coordinate(0, 1), Field.Cross))
        mapToCoordinate should contain((Coordinate(1, 1), Field.Empty))
      }
    }
  }

}
