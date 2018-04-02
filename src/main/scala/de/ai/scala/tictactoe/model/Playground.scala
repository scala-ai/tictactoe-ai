package de.ai.scala.tictactoe.model

case class Playground(
    values: Vector[Field],
    size: (Int, Int)
) {
  def mapToCoordinate: Set[((Int, Int), Field)] = {
    (0 until size._1).flatMap(row =>
      (0 until size._2).map(col =>
        ((row, col), values(row * size._2 + col))
      )
    )(collection.breakOut)
  }
}
