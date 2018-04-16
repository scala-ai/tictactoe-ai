package de.ai.scala.tictactoe.model

case class Playground(
    values: Vector[Field],
    dimensions: Int,
) {
  def mapToCoordinate: List[(Coordinate, Field)] = {
    (0 until dimensions).flatMap(row =>
      (0 until dimensions).map(col =>
        (Coordinate(col, row), values(row * dimensions + col))
      )
    )(collection.breakOut)
  }
}

object Playground {

  def fromCoordinateMap(map: Map[Coordinate, Field], dimensions: Int): Playground = {
    val values = for {
      x <- 0 until dimensions
      y <- 0 until dimensions
    } yield {
      map.getOrElse(Coordinate(x, y), Field.Empty)
    }

    Playground(values.toVector, dimensions)
  }
}
