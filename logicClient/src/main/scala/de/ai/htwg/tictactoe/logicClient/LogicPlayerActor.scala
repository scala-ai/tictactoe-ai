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
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategyBuilder
import de.ai.htwg.tictactoe.logicClient.LogicPlayerActor.PlayerReady
import grizzled.slf4j.Logging

object LogicPlayerActor {
  def props(strategyBuilder: TTTWinStrategyBuilder, random: Random, watchers: List[ActorRef]) = Props(new LogicPlayerActor(strategyBuilder, random, watchers))

  case object PlayerReady
}

class LogicPlayerActor private(strategyBuilder: TTTWinStrategyBuilder, random: Random, watchers: List[ActorRef]) extends Actor with Logging {

  case class GameScope(todo: List[GridPosition]) {
    def nextAction(random: Random): (GridPosition, GameScope) = {
      val action = todo(random.nextInt(todo.size))
      val newTodo = todo.filterNot(_ == action)
      (action, copy(todo = newTodo))
    }
  }

  override def receive: Receive = actorContext(GameScope(List()))

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
        if (possibleActions.contains(action)) {
          action
        } else {
          possibleActions(random.nextInt(possibleActions.size))
        }
      }
    )
  }

  private def handleSetGame(player: Player, gameControllerActor: ActorRef): Unit = {
    player match {
      case Player.Circle => gameControllerActor ! GameControllerMessages.RegisterCircle
      case Player.Cross => gameControllerActor ! GameControllerMessages.RegisterCross
    }
    val newGameScope = GameScope(possibleWinActions(random.nextInt(possibleWinActions.size)).list)
    context.become(actorContext(newGameScope))
    debug(s"New game scope: ${newGameScope.todo}")
    debug("logicPlayer is ready to play")
  }

  private val possibleWinActions = strategyBuilder.listAllWinStrategies
}
