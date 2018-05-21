package de.ai.htwg.tictactoe.clientConnection.gameController

import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition

trait GameControllerMultiPlayer extends GameControllerPlayer {

  def getXMoves(x: Int, field: GameField): List[GridPosition]

}
