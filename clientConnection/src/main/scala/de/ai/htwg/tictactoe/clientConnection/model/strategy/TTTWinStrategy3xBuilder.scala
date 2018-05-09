package de.ai.htwg.tictactoe.clientConnection.model.strategy

import scala.collection.mutable.ListBuffer

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition

object TTTWinStrategy3xBuilder extends TTTWinStrategyBuilder {
  override def dimensions: Int = 3
  override def listAllWinStrategies: List[TTTWinStrategy] = {
    def incrementRow(gp: GridPosition): GridPosition = gp.copy(x = gp.x + 1)
    def incrementColumn(gp: GridPosition): GridPosition = gp.copy(y = gp.y + 1)

    List(
      buildStrat(GridPosition(0, 0), incrementRow),
      buildStrat(GridPosition(0, 1), incrementRow),
      buildStrat(GridPosition(0, 2), incrementRow),

      buildStrat(GridPosition(0, 0), incrementColumn),
      buildStrat(GridPosition(1, 0), incrementColumn),
      buildStrat(GridPosition(2, 0), incrementColumn),

      buildStrat(GridPosition(0, 0), gp => gp.copy(x = gp.x + 1, y = gp.y + 1)),
      buildStrat(GridPosition(0, 2), gp => gp.copy(x = gp.x + 1, y = gp.y - 1)),
    )
  }

  private def buildStrat(start: GridPosition, mutator: GridPosition => GridPosition): TTTWinStrategy = {
    val builder = ListBuffer[GridPosition]()
    def rec(current: GridPosition): TTTWinStrategy = if (checkDimensions(current)) {
      builder += current
      rec(mutator(current))
    } else {
      TTTWinStrategy(builder.result(), 3)
    }

    rec(start)
  }


}
