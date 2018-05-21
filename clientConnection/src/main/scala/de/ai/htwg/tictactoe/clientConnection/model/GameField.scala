package de.ai.htwg.tictactoe.clientConnection.model


case class GameField private[model](
    gameState: GameField.GameState,
    gfDimensions: GameFieldDimensions,
    gameField: Map[GridPosition, Player],
    noPosMoves: Int,
) extends GameFieldDimensions {
  override val listAllGridPos: List[GridPosition] = gfDimensions.listAllGridPos
  override val dimensions: Int = gfDimensions.dimensions

  def finishGame(winner: Option[Player]): GameField = copy(gameState = GameField.Finished(winner))

  def isFinished: Boolean = gameState.isFinished

  def posIsSet(pos: GridPosition): Boolean = gameField.contains(pos)

  def isCurrentPlayer(p: Player): Boolean = gameState.asRunning.exists(_.current == p)

  def setPos(x: Int, y: Int): GameField = {
    setPos(GridPosition(x, y))
  }

  def setPos(pos: GridPosition): GameField = {
    gameState match {
      case GameField.Finished(_) => this
      case GameField.Running(player) =>
        val updatedGameField = this.gameField + (pos -> player)
        copy(
          noPosMoves = noPosMoves - 1,
          gameField = updatedGameField,
          gameState = GameField.Running(Player.other(player)),
        )
    }
  }

  def getAllEmptyPos: List[GridPosition] = listAllGridPos.filterNot(gameField.contains)

  def isCompletelyFilled(): Boolean = noPosMoves == 0

  def finishedUndecided: Boolean = gameState.asFinished.exists(_.winner.isEmpty)

  def getPos(pos: GridPosition): Option[Player] = gameField.get(pos)

  def foreach[U](func: (GridPosition, Player) => U): Unit = gameField.foreach(t => func(t._1, t._2))

  def fieldHash: Int = gameField
    .map(g => (g._1.x, g._1.y, g._2))
    .toSet
    .hashCode()

  def print(): String = {
    0.until(dimensions).map { y =>
      0.until(dimensions).map { x =>
        getPos(GridPosition(x, y)) match {
          case None => " - "
          case Some(Player.Circle) => " o "
          case Some(Player.Cross) => " x "
        }
      }.mkString
    }.mkString("\n")
  }

}

object GameField {

  def apply(startingPlayer: Player, stratBuilder: GameFieldDimensions): GameField = GameField(Running(startingPlayer), stratBuilder, Map(),
    stratBuilder.dimensions * stratBuilder.dimensions)

  sealed trait GameState {
    def asRunning: Option[Running] = None

    def asFinished: Option[Finished] = None

    def isRunning: Boolean = asRunning.isDefined

    def isFinished: Boolean = asFinished.isDefined
  }
  case class Running(current: Player) extends GameState {
    override lazy val asRunning = Some(this)
  }
  case class Finished(winner: Option[Player]) extends GameState {
    override lazy val asFinished = Some(this)
  }
}


