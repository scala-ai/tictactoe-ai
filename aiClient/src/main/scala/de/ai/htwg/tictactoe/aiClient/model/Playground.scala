package de.ai.htwg.tictactoe.aiClient.model

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition

case class Playground(
    values: Vector[Field],
    dimensions: Int,
) {
  def mapToCoordinate: List[(GridPosition, Field)] = {
    (0 until dimensions).flatMap(row =>
      (0 until dimensions).map(col =>
        (GridPosition(dimensions)(col, row), values(row * dimensions + col))
      )
    )(collection.breakOut)
  }
}

object Playground {

  def fromCoordinateMap(map: Map[GridPosition, Field], dimensions: Int): Playground = {
    val values = for {
      x <- 0 until dimensions
      y <- 0 until dimensions
    } yield {
      map.getOrElse(GridPosition(dimensions)(x, y), Field.Empty)
    }

    Playground(values.toVector, dimensions)
  }
}
