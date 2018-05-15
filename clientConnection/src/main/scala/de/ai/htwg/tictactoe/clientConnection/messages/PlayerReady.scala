package de.ai.htwg.tictactoe.clientConnection.messages

import de.ai.htwg.tictactoe.clientConnection.model.Player

case class PlayerReady(winner: Option[Player])
