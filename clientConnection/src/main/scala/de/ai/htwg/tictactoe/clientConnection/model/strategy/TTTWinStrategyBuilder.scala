package de.ai.htwg.tictactoe.clientConnection.model.strategy

import scala.collection.mutable.ListBuffer

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition

trait TTTWinStrategyBuilder {
  def dimensions: Int

  def listAllWinStrategies: List[TTTWinStrategy]

  protected def listAllGridPos: List[GridPosition] = {
    (for {
      x <- 0 until dimensions
      y <- 0 until dimensions
    } yield {
      GridPosition(x, y)
    }).toList
  }

  protected def checkDimensions(pos: GridPosition): Boolean = {
    def check(i: Int) = i >= 0 && i < dimensions
    check(pos.x) && check(pos.y)
  }

  private def buildAllWinStrategiesForPos(list: List[TTTWinStrategy]): Map[GridPosition, List[TTTWinStrategy]] = {
    val map = listAllGridPos.map(_ -> ListBuffer[TTTWinStrategy]()).toMap
    for {
      ws <- list
      pos <- ws.list
    } {
      map(pos) += ws
    }
    map.mapValues(_.result())
  }

  def allWinStrategyCheckerPerPos: Map[GridPosition, List[TTTWinStrategy]] = buildAllWinStrategiesForPos(listAllWinStrategies)

}

