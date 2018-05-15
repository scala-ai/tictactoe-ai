package de.ai.htwg.tictactoe.logicClient

import scala.util.Random

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerGameFinished
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerReady
import de.ai.htwg.tictactoe.gameLogic.messages.RegisterGame
import grizzled.slf4j.Logging


object RandomPlayerActor {
  def props(random: Random, watchers: List[ActorRef]) = Props(new RandomPlayerActor(random, watchers))
}

class RandomPlayerActor private(random: Random, watchers: List[ActorRef]) extends Actor with Logging {

  override def receive: Receive = {

    case RegisterGame(p, game, _) =>
      game.subscribe(new RandomPlayer(p, random, self))

    case PlayerGameFinished(winner) =>
      debug(s"RandomPlayer: Game finished")
      watchers.foreach(_ ! PlayerReady(winner))
  }
}
