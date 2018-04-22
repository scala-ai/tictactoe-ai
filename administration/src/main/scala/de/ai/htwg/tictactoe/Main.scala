package de.ai.htwg.tictactoe

import akka.actor.ActorSystem
import de.ai.htwg.tictactoe.clientConnection.fxUI.ClientMainActor
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor
import de.ai.htwg.tictactoe.playerClient.PlayerUiActor

object Main extends App {
  val system = ActorSystem()

  val dimensions = 4

  val gameName = "game1"
  val clientMain = system.actorOf(ClientMainActor.props(dimensions), "clientMain")
  val game = system.actorOf(GameControllerActor.props(dimensions), gameName)
  val cross = system.actorOf(PlayerUiActor.props(Player.Cross, clientMain, game, gameName))
  val circle = system.actorOf(PlayerUiActor.props(Player.Circle, clientMain, game, gameName))


}
