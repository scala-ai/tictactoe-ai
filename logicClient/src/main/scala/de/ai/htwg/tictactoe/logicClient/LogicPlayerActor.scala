package de.ai.htwg.tictactoe.logicClient

import scala.util.Random

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.messages.RegisterGame
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.logicClient.LogicPlayerActor.PlayerReady
import grizzled.slf4j.Logging

object LogicPlayerActor {
  def props(random: Random, watchers: List[ActorRef]) = Props(new LogicPlayerActor(random, watchers))

  case object PlayerReady
}

class LogicPlayerActor private(random: Random, watchers: List[ActorRef]) extends Actor with Logging {

  case class GameScope(todo: Set[(Int, Int)]) {
    def nextAction(random: Random): ((Int, Int), GameScope) = {
      val action = todo.toVector(random.nextInt(todo.size))
      val newTodo = todo.filter(!_.eq(action))
      (action, copy(todo = newTodo))
    }
  }

  override def receive: Receive = actorContext(GameScope(Set()))

  def actorContext(gameScope: GameScope): Receive = {
    case RegisterGame(p, game) => handleSetGame(p, game)

    case GameControllerMessages.GameUpdated(_) => trace("LogicPlayer: Game updated") // not interesting
    case GameControllerMessages.GameFinished(_, _) => trace("LogicPlayer: Game finished") // not interesting
    case GameControllerMessages.PosAlreadySet(_: GridPosition) => error(s"LogicPlayer: Pos already set")
    case GameControllerMessages.NotYourTurn(_: GridPosition) => error(s"LogicPlayer: Not your turn")
    case GameControllerMessages.YourTurn(gf: GameField) => doGameAction(gameScope, gf, sender())

    case GameControllerMessages.YourResult(_, result) =>
      debug(s"LogicPlayer: Game finished, result: $result")
      watchers.foreach(_ ! PlayerReady)
  }

  private def doGameAction(gameScope: GameScope, gf: GameField, gameControllerActor: ActorRef): Unit = {
    val possibleActions = gf.getAllEmptyPos
    gameControllerActor ! GameControllerMessages.SetPos(
      if (gameScope.todo.isEmpty) {
        possibleActions(random.nextInt(possibleActions.size))
      } else {
        val (action, newGameScope) = gameScope.nextAction(random)
        context.become(actorContext(newGameScope))
        possibleActions.collectFirst({
          case p if p.x == action._1 && p.y == action._2 => p
        }).fold(possibleActions(random.nextInt(possibleActions.size)))(e => e)
      }
    )
  }

  private def handleSetGame(player: Player, gameControllerActor: ActorRef): Unit = {
    player match {
      case Player.Circle => gameControllerActor ! GameControllerMessages.RegisterCircle
      case Player.Cross => gameControllerActor ! GameControllerMessages.RegisterCross
    }
    val newGameScope = GameScope(possibleWinActions(random.nextInt(possibleWinActions.size)))
    context.become(actorContext(newGameScope))
    debug(s"New game scope: ${newGameScope.todo}")
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
