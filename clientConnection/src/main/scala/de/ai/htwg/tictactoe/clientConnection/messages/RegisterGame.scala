package de.ai.htwg.tictactoe.clientConnection.messages

import akka.actor.ActorRef
import de.ai.htwg.tictactoe.clientConnection.model.Player

case class RegisterGame(player: Player, gameControllerActor: ActorRef)