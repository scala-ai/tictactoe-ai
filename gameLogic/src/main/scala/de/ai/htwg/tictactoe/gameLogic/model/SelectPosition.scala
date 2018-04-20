package de.ai.htwg.tictactoe.gameLogic.model

import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition

case class SelectPosition(p: Player, pos: GridPosition)
