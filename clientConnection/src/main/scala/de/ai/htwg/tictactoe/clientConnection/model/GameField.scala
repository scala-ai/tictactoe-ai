package de.ai.htwg.tictactoe.clientConnection.model

case class GameField private[model](
    gameState: GameField.GameState,
    dimensions: Int,
    gameField: Map[GridPosition, Player] = Map.empty,
) {

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
          gameField = updatedGameField,
          gameState = GameField.Running(Player.other(player)),
        )
    }
  }

  def getAllEmptyPos: List[GridPosition] = GameField.getAllEmptyPos(dimensions, gameField)

  def isCompletelyFilled(): Boolean = GameField.isCompletelyFilled(dimensions, gameField)

  def finishedUndecided: Boolean = gameState.asFinished.exists(_.winner.isEmpty)

  def getPos(pos: GridPosition): Option[Player] = gameField.get(pos)

  def foreach[U](func: (GridPosition, Player) => U): Unit = gameField.foreach(t => func(t._1, t._2))

  def fieldHash: Int = gameField
    .map(g => (g._1.x, g._1.y, g._2))
    .toSet
    .hashCode()
}

object GameField {

  def apply(startingPlayer: Player, dimensions: Int): GameField = GameField(Running(startingPlayer), dimensions, Map())

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

  def getAllEmptyPos(dimensions: Int, gf: Map[GridPosition, Player]): List[GridPosition] = {
    val seq: Seq[GridPosition] = for {
      x <- 0 until dimensions
      y <- 0 until dimensions
      gp = GridPosition(x, y)
      if !gf.contains(gp)
    } yield {
      gp
    }
    seq.toList
  }

  def isCompletelyFilled(dimensions: Int, gameField: Map[GridPosition, Player]): Boolean = gameField.size >= dimensions * dimensions
}


