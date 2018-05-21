package de.ai.htwg.tictactoe.clientConnection.model.strategy

import scala.collection.mutable.ListBuffer

import de.ai.htwg.tictactoe.clientConnection.model.GameFieldDimensions
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition

trait TTTWinStrategyBuilder extends GameFieldDimensions {

  override val listAllGridPos: List[GridPosition] = super.listAllGridPos
  def listAllWinStrategies: List[TTTWinStrategy]

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

  lazy val allWinStrategyCheckerPerPos: Map[GridPosition, List[TTTWinStrategy]] = buildAllWinStrategiesForPos(listAllWinStrategies)

}

