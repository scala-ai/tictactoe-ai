package de.ai.htwg.tictactoe.playerClient

import scala.util.Random

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.playerClient.LogicPlayerActor.RegisterGame
import grizzled.slf4j.Logging

object LogicPlayerActor {
  def props(dimensions: Int, random: Random) = Props(new LogicPlayerActor(dimensions, random))

  case class RegisterGame(player: Player, gameControllerActor: ActorRef)
}

class LogicPlayerActor private(dimensions: Int, random: Random) extends Actor with Logging {

  case class GameScope(todo: Set[(Int, Int)]) {
    def nextAction(random: Random): ((Int, Int), GameScope) = {
      val action = todo.toVector(random.nextInt(todo.size))
      val newTodo = todo.filter(!_.eq(action))
      (action, copy(todo = newTodo))
    }
  }

  var gameScope = GameScope(Set())

  override def receive: Receive = {
    case RegisterGame(p, game) => handleSetGame(p, game)
    case GameControllerMessages.YourTurn(gf: GameField) => doGameAction(gf, sender())
  }

  private def doGameAction(gf: GameField, ref: ActorRef): Unit = {
    val (action, newGameScope) = gameScope.nextAction(random)
    val possibleActions = gf.getAllEmptyPos
    gameScope = newGameScope
    possibleActions.collectFirst({
      case p if p.x == action._1 && p.y == action._2 => p
    }).map(p => {
      info(s"Choose action $p")
      p
    }).fold(possibleActions(random.nextInt(possibleActions.size)))
  }

  private def handleSetGame(player: Player, gameControllerActor: ActorRef): Unit = {
    player match {
      case Player.Circle => gameControllerActor ! GameControllerMessages.RegisterCircle
      case Player.Cross => gameControllerActor ! GameControllerMessages.RegisterCross
    }
    gameScope = GameScope(possibleWinActions(random.nextInt(possibleWinActions.size)))
    info(s"New game scope: ${gameScope.todo}")
    debug("logicPlayer is ready to play")
  }

  private val possibleWinActions = List(
    // horizontal
    Set((0, 0), (0, 1), (0, 2), (0, 3)),
    Set((1, 0), (1, 1), (1, 2), (1, 3)),
    Set((2, 0), (2, 1), (2, 2), (2, 3)),
    Set((3, 0), (3, 1), (3, 2), (3, 3)),
    // vertical
    Set((0, 0), (1, 0), (2, 0), (3, 0)),
    Set((0, 1), (1, 1), (2, 1), (3, 1)),
    Set((0, 2), (1, 2), (2, 2), (3, 2)),
    Set((0, 3), (1, 3), (2, 3), (3, 3)),
    // diagonal
    Set((0, 0), (1, 1), (2, 2), (3, 3)),
    Set((3, 0), (2, 1), (1, 2), (0, 3)),
    // squares
    Set((0, 0), (0, 1), (1, 0), (1, 1)),
    Set((1, 0), (1, 1), (2, 0), (2, 1)),
    Set((2, 0), (2, 1), (3, 0), (3, 1)),
    Set((0, 1), (0, 2), (1, 1), (1, 2)),
    Set((1, 1), (1, 2), (2, 1), (2, 2)),
    Set((2, 1), (2, 2), (3, 1), (3, 2)),
    Set((0, 2), (0, 3), (1, 2), (1, 3)),
    Set((1, 2), (1, 3), (2, 2), (2, 3)),
    Set((2, 2), (2, 3), (3, 2), (3, 3))
  )
}
