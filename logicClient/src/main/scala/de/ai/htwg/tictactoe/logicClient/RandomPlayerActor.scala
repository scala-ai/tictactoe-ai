package de.ai.htwg.tictactoe.logicClient

import scala.util.Random

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerReady
import de.ai.htwg.tictactoe.clientConnection.messages.RegisterGame
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import grizzled.slf4j.Logging


object RandomPlayerActor {
  def props(random: Random, watchers: List[ActorRef]) = Props(new RandomPlayerActor(random, watchers))
}

class RandomPlayerActor private(random: Random, watchers: List[ActorRef]) extends Actor with Logging {

  override def receive: Receive = {
    case RegisterGame(p, game) => handleSetGame(p, game)

    case GameControllerMessages.GameUpdated(_) => trace("RandomPlayer: Game updated") // not interesting
    case GameControllerMessages.GameFinished(_, _) => trace("RandomPlayer: Game finished") // not interesting
    case GameControllerMessages.PosAlreadySet(_: GridPosition) => error(s"RandomPlayer: Pos already set")
    case GameControllerMessages.NotYourTurn(_: GridPosition) => error(s"RandomPlayer: Not your turn")
    case GameControllerMessages.YourTurn(gf: GameField) => doGameAction(gf, sender())

    case GameControllerMessages.YourResult(_, result) =>
      debug(s"RandomPlayer: Game finished, result: $result")
      watchers.foreach(_ ! PlayerReady)
  }

  private def doGameAction(gf: GameField, gameControllerActor: ActorRef): Unit = {
    val possibleActions = gf.getAllEmptyPos
    gameControllerActor ! GameControllerMessages.SetPos(
      possibleActions(random.nextInt(possibleActions.size))
    )
  }

  private def handleSetGame(player: Player, gameControllerActor: ActorRef): Unit = {
    player match {
      case Player.Circle => gameControllerActor ! GameControllerMessages.RegisterCircle
      case Player.Cross => gameControllerActor ! GameControllerMessages.RegisterCross
    }
    debug("RandomPlayer: Ready to play")
  }
}
