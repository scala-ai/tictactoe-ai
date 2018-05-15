package de.ai.htwg.tictactoe.logicClient

import scala.util.Random

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerGameFinished
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerReady
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategyBuilder
import de.ai.htwg.tictactoe.gameLogic.messages.RegisterGame
import grizzled.slf4j.Logging

object LogicPlayerActor {
  def props(strategyBuilder: TTTWinStrategyBuilder, random: Random, watchers: List[ActorRef]) = Props(new LogicPlayerActor(strategyBuilder, random, watchers))
}

class LogicPlayerActor private(strategyBuilder: TTTWinStrategyBuilder, random: Random, watchers: List[ActorRef]) extends Actor with Logging {

  override def receive: Receive = {
    case RegisterGame(p, game, _) =>
      game.subscribe(new LogicPlayer(p, random, self, possibleWinActions))
      debug("LogicPlayer: Ready to play")


    case PlayerGameFinished(winner) =>
      debug(s"LogicPlayer: Game finished")
      watchers.foreach(_ ! PlayerReady(winner))
  }

  private val possibleWinActions: List[TTTWinStrategy] = strategyBuilder.listAllWinStrategies
}
