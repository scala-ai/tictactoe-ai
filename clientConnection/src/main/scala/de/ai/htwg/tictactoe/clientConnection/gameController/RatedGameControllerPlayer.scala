package de.ai.htwg.tictactoe.clientConnection.gameController

import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition

object RatedGameControllerPlayer {
  case class RatedMove(move: GridPosition, score: Double)
}

trait RatedGameControllerPlayer extends GameControllerPlayer {
  def getScores(gameField: GameField, moves: List[GridPosition]): List[RatedGameControllerPlayer.RatedMove]
}