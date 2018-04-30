package de.ai.htwg.tictactoe

import akka.actor.ActorSystem
import de.ai.htwg.tictactoe.aiClient.AiActor
import de.ai.htwg.tictactoe.aiClient.AiActor.RegisterGame
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMainActor
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor
import de.ai.htwg.tictactoe.playerClient.PlayerUiActor

object AiMain extends App {
  val system = ActorSystem()

  val dimensions = 4

  val gameName = "game1"
  val clientMain = system.actorOf(UiMainActor.props(dimensions), "clientMain")
  val game = system.actorOf(GameControllerActor.props(dimensions, Player.Cross), gameName)
  val cross = system.actorOf(PlayerUiActor.props(Player.Cross, clientMain, game, gameName))
  val circle = system.actorOf(AiActor.props())
  circle ! RegisterGame(Player.Circle, game)
}
