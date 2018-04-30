package de.ai.htwg.tictactoe.playerClient

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import de.ai.htwg.tictactoe.clientConnection.fxUI.GameUiActor
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMainActor
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import grizzled.slf4j.Logging

object PlayerUiActor {
  def props(player: Player, clientMainActor: ActorRef, gameControllerActor: ActorRef, gameName: String) =
    Props(new PlayerUiActor(player, clientMainActor, gameControllerActor, gameName))
}

class PlayerUiActor private(player: Player, clientMainActor: ActorRef, gameControllerActor: ActorRef, gameName: String)
  extends Actor with Logging {
  private case class SelectPos(pos: GridPosition)

  player match {
    case Player.Circle => gameControllerActor ! GameControllerMessages.RegisterCircle
    case Player.Cross => gameControllerActor ! GameControllerMessages.RegisterCross
  }
  clientMainActor ! UiMainActor.CreateGameUI(s"$gameName-$player")

  private def handleMouseEvent(pos: GridPosition): Unit = {
    self ! SelectPos(pos)
  }

  class MainState(uiActor: ActorRef) extends Receive {
    override def isDefinedAt(x: Any): Boolean = pf.isDefinedAt(x)

    override def apply(v1: Any): Unit = pf.apply(v1)

    val pf: Receive = {
      case SelectPos(pos) => gameControllerActor ! GameControllerMessages.SetPos(pos)
      case GameControllerMessages.PosAlreadySet(_: GridPosition) => debug("position already set")
      case GameControllerMessages.NotYourTurn(_: GridPosition) => debug("not your turn")
      case GameControllerMessages.PositionSet(gf: GameField) => uiActor ! GameUiActor.PrintField(gf)
      case GameControllerMessages.GameWon(winner: Player, gf: GameField) =>
        debug(s"winner: $winner")
        uiActor ! GameUiActor.PrintField(gf)
    }

    uiActor ! GameUiActor.SubscribeToMouseEvents(handleMouseEvent)
  }

  def receivePreStart: Receive = {
    case UiMainActor.ReturnGameUI(ref) => context.become(new MainState(ref))
  }

  override def receive: Receive = receivePreStart
}
