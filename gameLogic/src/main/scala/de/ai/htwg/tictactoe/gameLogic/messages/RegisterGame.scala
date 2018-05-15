package de.ai.htwg.tictactoe.gameLogic.messages

import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController

case class RegisterGame(player: Player, gameController: GameFieldController, training: Boolean)