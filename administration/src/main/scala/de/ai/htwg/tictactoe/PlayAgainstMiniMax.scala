package de.ai.htwg.tictactoe

import de.ai.htwg.tictactoe.clientConnection.gameController.GameController
import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.logicClient.MiniMaxBehindAiPlayer
import de.ai.htwg.tictactoe.logicClient.RandomPlayer

object PlayAgainstMiniMax extends PlayAgainstUi {
  override def buildOpponent(gameController: GameController): GameControllerPlayer = {
    new MiniMaxBehindAiPlayer(Player.Cross, strategy, new RandomPlayer(Player.Cross, random))
  }

  start()
}
