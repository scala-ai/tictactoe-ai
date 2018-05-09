package de.ai.htwg.tictactoe.playerClient

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.PoisonPill
import de.ai.htwg.tictactoe.clientConnection.fxUI.GameUiActor
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMainActor
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPositionOLD
import de.ai.htwg.tictactoe.clientConnection.model.Player
import grizzled.slf4j.Logging

object PlayerUiActor {
  def props(player: Player, clientMainActor: ActorRef, gameControllerActor: ActorRef, gameName: String) =
    Props(new PlayerUiActor(player, clientMainActor, gameControllerActor, gameName))

  case object Euthanize
}

class PlayerUiActor private(player: Player, clientMainActor: ActorRef, gameControllerActor: ActorRef, gameName: String)
  extends Actor with Logging {
  private case class SelectPos(pos: GridPositionOLD)

  player match {
    case Player.Circle => gameControllerActor ! GameControllerMessages.RegisterCircle
    case Player.Cross => gameControllerActor ! GameControllerMessages.RegisterCross
  }
  clientMainActor ! UiMainActor.CreateGameUI(s"$gameName-$player")

  private def handleMouseEvent(pos: GridPositionOLD): Unit = {
    self ! SelectPos(pos)
  }

  class MainState(uiActor: ActorRef) extends Receive {
    override def isDefinedAt(x: Any): Boolean = pf.isDefinedAt(x)

    override def apply(v1: Any): Unit = pf.apply(v1)

    val pf: Receive = {
      case PlayerUiActor.Euthanize =>
        uiActor ! PoisonPill
        self ! PoisonPill
      case SelectPos(pos) => gameControllerActor ! GameControllerMessages.SetPos(pos)
      case GameControllerMessages.PosAlreadySet(_: GridPositionOLD) => debug("position already set")
      case GameControllerMessages.NotYourTurn(_: GridPositionOLD) => debug("not your turn")
      case GameControllerMessages.GameUpdated(gf: GameField) => uiActor ! GameUiActor.PrintField(gf)
      case GameControllerMessages.GameFinished(gf: GameField, winner) =>
        winner match {
          case Some(w) => debug(s"winner: $w")
          case None => debug("game draw")
        }

        uiActor ! GameUiActor.PrintField(gf)
    }

    uiActor ! GameUiActor.SubscribeToMouseEvents(handleMouseEvent)
  }

  def receivePreStart: Receive = {
    case UiMainActor.ReturnGameUI(ref) => context.become(new MainState(ref))
  }
  override def receive: Receive = receivePreStart
}
