package de.ai.htwg.tictactoe.logicClient

import scala.util.Random

import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import grizzled.slf4j.Logging

class RandomPlayer(
    override val currentPlayer: Player,
    random: Random,
) extends GameControllerPlayer with Logging {
  trace(s"RandomPlayer starts playing as $currentPlayer")

  override def getMove(gf: GameField): GridPosition = {
    trace("RandomPlayer: current turn")
    val possibleActions = gf.getAllEmptyPos
    possibleActions(random.nextInt(possibleActions.size))
  }
}
