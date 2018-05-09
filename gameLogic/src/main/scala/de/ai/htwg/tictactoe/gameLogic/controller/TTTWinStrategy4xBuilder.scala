package de.ai.htwg.tictactoe.gameLogic.controller

import scala.collection.mutable.ListBuffer

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition

object TTTWinStrategy4xBuilder extends TTTWinStrategyBuilder {
  val requiredConnectedToWin: Int = 4
  override def dimensions: Int = 4
  override def listAllWinStrategies: List[TTTWinStrategy] = {
    def incrementRow(gp: GridPosition): GridPosition = gp.copy(x = gp.x + 1)
    def incrementColumn(gp: GridPosition): GridPosition = gp.copy(y = gp.y + 1)

    List(
      buildStrat(GridPosition(0, 0), incrementRow),
      buildStrat(GridPosition(0, 1), incrementRow),
      buildStrat(GridPosition(0, 2), incrementRow),
      buildStrat(GridPosition(0, 3), incrementRow),

      buildStrat(GridPosition(0, 0), incrementColumn),
      buildStrat(GridPosition(1, 0), incrementColumn),
      buildStrat(GridPosition(2, 0), incrementColumn),
      buildStrat(GridPosition(3, 0), incrementColumn),

      buildStrat(GridPosition(0, 0), gp => gp.copy(x = gp.x + 1, y = gp.y + 1)),
      buildStrat(GridPosition(0, 3), gp => gp.copy(x = gp.x + 1, y = gp.y - 1)),
    ) ++ listAllGridPos.flatMap(buildOptSquareStrat)
  }

  private def buildOptSquareStrat(start: GridPosition): Option[TTTWinStrategy] = {
    val list = List(
      start,
      start.copy(x = start.x + 1),
      start.copy(y = start.y + 1),
      start.copy(x = start.x + 1, y = start.y + 1),
    ).filter(checkDimensions)
    if (list.size != requiredConnectedToWin) None else Some(TTTWinStrategy(list, requiredConnectedToWin))
  }

  private def buildStrat(start: GridPosition, mutator: GridPosition => GridPosition): TTTWinStrategy = {
    val builder = ListBuffer[GridPosition]()
    def rec(current: GridPosition): TTTWinStrategy = if (checkDimensions(current)) {
      builder += current
      rec(mutator(current))
    } else {
      TTTWinStrategy(builder.result(), requiredConnectedToWin)
    }

    rec(start)
  }

}
