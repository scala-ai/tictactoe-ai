package de.ai.htwg.tictactoe.clientConnection.model

trait GameFieldDimensions {
  def dimensions: Int

  def listAllGridPos: List[GridPosition] = {
    (for {
      x <- 0 until dimensions
      y <- 0 until dimensions
    } yield {
      GridPosition(x, y)
    }).toList
  }


}
