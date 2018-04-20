package de.ai.htwg.tictactoe.clientConnection.model

class GameField private[model](
    private[model] val current: Player,
    private[model] val gameField: Map[GridPosition, Player] = Map.empty,
    val isFinished: Boolean = false,
) {

  def posIsSet(pos: GridPosition): Boolean = gameField.contains(pos)
  def isCurrentPlayer(p: Player): Boolean = p == current

  def setPos(pos: GridPosition): GameField = {
    require(!isFinished, "Game is already finished") // FIXME solve in return type. Don't throw an exception here.
    val updatedGameField = this.gameField + (pos -> current)
    val finished = pos.buildCombinationsOf4.exists(GameField.isWinCondition(updatedGameField, current))
    new GameField(
      Player.other(current),
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


  def apply(startingPlayer: Player): GameField = {
    new GameField(startingPlayer)
  }
}
