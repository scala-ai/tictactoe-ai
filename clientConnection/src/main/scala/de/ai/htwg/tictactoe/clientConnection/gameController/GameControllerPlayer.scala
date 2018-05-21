package de.ai.htwg.tictactoe.clientConnection.gameController

import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player

trait GameControllerPlayer {

  def currentPlayer: Player

  def getMove(field: GameField): GridPosition

}
