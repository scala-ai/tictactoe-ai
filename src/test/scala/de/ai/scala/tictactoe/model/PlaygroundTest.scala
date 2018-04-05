package de.ai.scala.tictactoe.model

import org.scalatest.FreeSpec
import org.scalatest.Matchers

class PlaygroundTest extends FreeSpec with Matchers {

  "A playground" - {
    "should return its values mapped to their coordinates" - {
      "in a 2x2 playground" in {
        val playground = Playground(
          Vector(
            EmptyField(), CircleField(),
            CrossField(), EmptyField()
          ), (2, 2)
        )
        val mapToCoordinate = playground.mapToCoordinate
        println(mapToCoordinate)

        mapToCoordinate.size shouldBe 4
        mapToCoordinate should contain((Coordinate(0, 0), EmptyField()))
        mapToCoordinate should contain((Coordinate(1, 0), CircleField()))
        mapToCoordinate should contain((Coordinate(0, 1), CrossField()))
        mapToCoordinate should contain((Coordinate(1, 1), EmptyField()))
      }
    }
  }

}
