package de.ai.htwg.tictactoe.clientConnection.model

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
    val finished = pos.buildConnectedCombinations.exists(GameField.isWinCondition(updatedGameField, current))
    new GameField(
      if (finished) current else Player.other(current),
      dimensions,
      updatedGameField,
      finished,
    )
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
