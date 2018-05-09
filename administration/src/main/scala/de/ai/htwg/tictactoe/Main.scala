package de.ai.htwg.tictactoe

import akka.actor.ActorSystem
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMainActor
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor
import de.ai.htwg.tictactoe.gameLogic.controller.TTTWinStrategy4xBuilder
import de.ai.htwg.tictactoe.playerClient.PlayerUiActor

object Main extends App {
  val system = ActorSystem()

  val strategy = TTTWinStrategy4xBuilder

  val gameName = "game1"
  val clientMain = system.actorOf(UiMainActor.props(strategy.dimensions), "clientMain")
  val game = system.actorOf(GameControllerActor.props(Player.Circle, strategy), gameName)
  val cross = system.actorOf(PlayerUiActor.props(Player.Cross, clientMain, game, gameName))
  val circle = system.actorOf(PlayerUiActor.props(Player.Circle, clientMain, game, gameName))
}
