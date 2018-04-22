package de.ai.htwg.tictactoe.clientConnection.model

import scala.annotation.tailrec

class GameField private[model](
    private[model] val current: Player,
    val dimensions: Int,
    val gameField: Map[GridPosition, Player] = Map.empty,
    val isFinished: Boolean = false,
) {

  val posBuilder = GridPosition(dimensions)

  def posIsSet(pos: GridPosition): Boolean = gameField.contains(pos)
  def isCurrentPlayer(p: Player): Boolean = p == current

  def setPos(x: Int, y: Int): GameField = {
    setPos(posBuilder(x, y))
  }

  def setPos(pos: GridPosition): GameField = {
    require(!isFinished, "Game is already finished") // FIXME solve in return type. Don't throw an exception here.
    val updatedGameField = this.gameField + (pos -> current)
    val finished = pos.buildConnectedCombinations.exists(checkIsWinCondition(updatedGameField))
    new GameField(
      if (finished) current else Player.other(current),
      dimensions,
      updatedGameField,
      finished,
    )
  }

  private def checkIsWinCondition(updatedGameField: Map[GridPosition, Player])(list: List[GridPosition]): Boolean = {
    @tailrec def checkLengthMaxConnectedList(playerList: List[Option[Player]], building: Int, longest: Int): Int = playerList match {
      case Nil => math.max(longest, building)
      case Some(this.current) :: tail => checkLengthMaxConnectedList(tail, building + 1, longest)
      case _ :: tail => checkLengthMaxConnectedList(tail, 0, math.max(longest, building))
    }

    checkLengthMaxConnectedList(list.map(updatedGameField.get), 0, 0) >= GameField.noConnectedFieldRequiredToWin
  }

}

object GameField {
  private val noConnectedFieldRequiredToWin = 4

  private[GameField] def isWinCondition(gameField: Map[GridPosition, Player], current: Player)(list: List[GridPosition]): Boolean = {
    list.flatMap(gameField.get).count(player => player == current) == GameField.noConnectedFieldRequiredToWin
  }


  def apply(startingPlayer: Player, dimensions: Int): GameField = {
    new GameField(startingPlayer, dimensions)
  }
}
