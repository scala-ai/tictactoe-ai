package de.ai.scala.tictactoe.model

case class Playground(
    values: Vector[Field],
    size: (Int, Int)
) {
  def mapToCoordinate: Set[(Coordinate, Field)] = {
    (0 until size._1).flatMap(row =>
      (0 until size._2).map(col =>
        (Coordinate(col, row), values(row * size._2 + col))
      )
    )(collection.breakOut)
  }
}
