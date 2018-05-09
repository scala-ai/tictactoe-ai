package de.ai.htwg.tictactoe.clientConnection.model

import scala.annotation.tailrec

class GameField private[model](
    private[model] val current: Player,
    val dimensions: Int,
    private[model] val gameField: Map[GridPositionOLD, Player] = Map.empty,
    val isFinished: Boolean = false,
) {

  val posBuilder = GridPositionOLD(dimensions)

  def posIsSet(pos: GridPositionOLD): Boolean = gameField.contains(pos)

  def isCurrentPlayer(p: Player): Boolean = p == current

  def setPos(x: Int, y: Int): GameField = {
    setPos(posBuilder(x, y))
  }

  def setPos(pos: GridPositionOLD): GameField = {
    require(!isFinished, "Game is already finished") // FIXME solve in return type. Don't throw an exception here.
    val updatedGameField = this.gameField + (pos -> current)
    val finished = pos.buildConnectedCombinations.exists(checkIsWinCondition(updatedGameField)) ||
      allPosSet(updatedGameField)
    new GameField(
      if (finished) current else Player.other(current),
      dimensions,
      updatedGameField,
      finished,
    )
  }

  private def checkIsWinCondition(updatedGameField: Map[GridPositionOLD, Player])(list: List[GridPositionOLD]): Boolean = {
    @tailrec def checkLengthMaxConnectedList(playerList: List[Option[Player]], building: Int, longest: Int): Int = playerList match {
      case Nil => math.max(longest, building)
      case Some(this.current) :: tail => checkLengthMaxConnectedList(tail, building + 1, longest)
      case _ :: tail => checkLengthMaxConnectedList(tail, 0, math.max(longest, building))
    }

    checkLengthMaxConnectedList(list.map(updatedGameField.get), 0, 0) >= GameField.noConnectedFieldRequiredToWin
  }

  def getAllEmptyPos: List[GridPositionOLD] = getAllEmptyPos(gameField)

  private def getAllEmptyPos(gf: Map[GridPositionOLD, Player]): List[GridPositionOLD] = {
    val positions = for {
      x <- 0 until dimensions
      y <- 0 until dimensions
    } yield {
      posBuilder(x, y)
    }

    positions.toList.filterNot(gf.contains)
  }

  def finishedUndecided: Boolean = isFinished && allPosSet()

  def allPosSet(): Boolean = allPosSet(gameField)

  private def allPosSet(gameField: Map[GridPositionOLD, Player]): Boolean = gameField.size >= dimensions * dimensions

  def getPos(pos: GridPositionOLD): Option[Player] = gameField.get(pos)

  def foreach[U](func: (GridPositionOLD, Player) => U): Unit = gameField.foreach(t => func(t._1, t._2))

  def fieldHash: Int = gameField
    .map(g => (g._1.x, g._1.y, g._2))
    .toSet
    .hashCode()
}

object GameField {
  private val noConnectedFieldRequiredToWin = 4

  private[GameField] def isWinCondition(gameField: Map[GridPositionOLD, Player], current: Player)(list: List[GridPositionOLD]): Boolean = {
    list.flatMap(gameField.get).count(player => player == current) == GameField.noConnectedFieldRequiredToWin
  }


  def apply(startingPlayer: Player, dimensions: Int): GameField = {
    new GameField(startingPlayer, dimensions)
  }
}
