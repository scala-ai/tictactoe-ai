package de.ai.htwg.tictactoe.clientConnection.fxUI

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition

object GameUiActor {
  def props(name: String, dimensions: Int) = Props(new GameUiActor(name, dimensions))

  case class PrintField(gameField: GameField)
  case object Clear
  case class SubscribeToMouseEvents(msgFactory: GridPosition => Any)
  case object Unsubscribe
}

private class GameUiActor(name: String, dimensions: Int) extends Actor {
  private case class SetStage()

  private case class Subscriber(actorRef: ActorRef, msgFactory: GridPosition => Any)

  private val futStage = GameUiStage(name, dimensions, handleMouseEvent)
  // the overhead for handling this completely async is a lot more than simply sleeping once.
  private val stage = Await.result(futStage, Duration.Inf)

  // state
  private var listeners: List[Subscriber] = Nil
  private var field: Option[GameField] = None

  stage.show()


  private def handleMouseEvent(pos: GridPosition): Unit = {
    if (field.flatMap(_.gameField.get(pos)).isEmpty) {
      listeners.foreach { sub =>
        sub.actorRef ! sub.msgFactory(pos)
      }

    }
  }

  private def clear(): Unit = {
    field = None
    stage.clear()
  }

  private def printField(field: GameField): Unit = {
    clear()
    this.field = Some(field)
    field.gameField.foreach {
      case (pos, Player.Cross) => stage.cross(pos).set()
      case (pos, Player.Circle) => stage.circle(pos).set()
    }
  }

  override def receive: Receive = {
    case GameUiActor.Clear => clear()
    case GameUiActor.PrintField(f) => printField(f)
    case GameUiActor.SubscribeToMouseEvents(fac) => listeners = Subscriber(sender(), fac) :: listeners
    case GameUiActor.Unsubscribe => listeners = listeners.filterNot(_.actorRef == sender())
  }

}
