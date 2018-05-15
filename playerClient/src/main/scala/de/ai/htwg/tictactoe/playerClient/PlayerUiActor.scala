package de.ai.htwg.tictactoe.playerClient

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.Stash
import de.ai.htwg.tictactoe.clientConnection.fxUI.GameUiStage
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMainActor
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerGameFinished
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerReady
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.util.DelegatedPartialFunction
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController
import grizzled.slf4j.Logging

object PlayerUiActor {
  def props(player: Player, clientMainActor: ActorRef, gameController: GameFieldController, gameName: String, watchers: ActorRef*) =
    Props(new PlayerUiActor(player, clientMainActor, gameController, gameName, watchers.toList))

  case object Euthanize
}

class PlayerUiActor private(player: Player, clientMainActor: ActorRef, gameController: GameFieldController, gameName: String, watchers: List[ActorRef])
  extends Actor with Stash with Logging {

  clientMainActor ! UiMainActor.CreateGameUI(s"$gameName-$player")

  class MainState(gameUi: GameUiStage) extends DelegatedPartialFunction[Any, Unit] {
    val uiPlayer = new UiPlayer(player, self, gameUi, gameController.getGrid())
    gameController.subscribe(uiPlayer)

    def pf: Receive = {
      case PlayerUiActor.Euthanize =>
        gameUi.stop()
        self ! PoisonPill

      case PlayerGameFinished(winner) =>
        watchers.foreach(_ ! PlayerReady(winner))
        val result = winner match {
          case None => GameControllerMessages.GameDraw
          case Some(`player`) => GameControllerMessages.GameWon
          case _ /* opponent */ => GameControllerMessages.GameLost
        }
        trace(s"Game finished with result: $result")
    }

  }


  def receivePreStart: Receive = {
    case UiMainActor.ReturnGameUI(gameUi) =>
      context.become(new MainState(gameUi))

    case msg =>
      debug(s"received msg: $msg")
      stash()
  }
  override def receive: Receive = receivePreStart
}
