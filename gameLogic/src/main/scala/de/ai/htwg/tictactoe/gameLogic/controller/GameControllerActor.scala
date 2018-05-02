package de.ai.htwg.tictactoe.gameLogic.controller

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContextExecutor

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.SelectPositionAck
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.RetCode

object GameControllerActor {
  def props(dimensions: Int, startingPlayer: Player) = Props(new GameControllerActor(dimensions, startingPlayer))
}

class GameControllerActor private(dimensions: Int, startingPlayer: Player) extends Actor {

  private class SubscriberList(var list: List[ActorRef] = Nil) {
    def !!(msg: Any): Unit = list.foreach { r => r ! msg }
    def add(subscriber: ActorRef): Unit = list = subscriber :: list.filterNot(_ == subscriber)
    def remove(subscriber: ActorRef): Unit = list = list.filterNot(_ == subscriber)
    def contains(subscriber: ActorRef): Boolean = list.contains(subscriber)
  }
  private class PlayerList(val player: Player) extends SubscriberList

  private case class PositionSelected(p: Player, pos: GridPosition, state: GameField, returnCode: RetCode, sender: ActorRef)

  private val RetCode = GameFieldControllerActor.RetCode
  private val gameFieldActor: ActorRef = context.actorOf(GameFieldControllerActor.props(startingPlayer = startingPlayer, dimensions))
  private val observerList: SubscriberList = new SubscriberList()
  private val playerListCircle: PlayerList = new PlayerList(Player.Circle)

  private val playerListCross: PlayerList = new PlayerList(Player.Cross)

  private var winner: Option[Player] = None

  private def playerToList(p: Player): PlayerList = p match {
    case Player.Circle => playerListCircle
    case Player.Cross => playerListCross
  }

  private def sendResultMessage(state: GameField, player: Player): Unit = {
    observerList !! GameControllerMessages.GameFinished(state, winner)
    winner match {
      case None =>
        playerListCross !! GameControllerMessages.YourResult(state, GameControllerMessages.GameDraw)
        playerListCircle !! GameControllerMessages.YourResult(state, GameControllerMessages.GameDraw)
      case Some(win) =>
        playerToList(win) !! GameControllerMessages.YourResult(state, GameControllerMessages.GameWon)
        playerToList(Player.other(win)) !! GameControllerMessages.YourResult(state, GameControllerMessages.GameLost)
    }
  }

  private def handleSelectPosAck(retCode: GameFieldControllerActor.RetCode, state: GameField, pos: GridPosition, player: Player, sender: ActorRef): Unit =
    retCode match {
      case RetCode.PositionAlreadySelected => sender ! GameControllerMessages.PosAlreadySet(pos)
      case RetCode.NotThisPlayersTurn => sender ! GameControllerMessages.NotYourTurn(pos)
      case RetCode.GameAlreadyFinished => observerList !! GameControllerMessages.GameFinished(state, winner)

      case RetCode.PositionSet =>
        observerList !! GameControllerMessages.GameUpdated(state)
        playerToList(Player.other(player)) !! GameControllerMessages.YourTurn(state)

      case RetCode.GameWon =>
        winner = Some(player)
        sendResultMessage(state, player)

      case RetCode.GameUndecided =>
        winner = None
        sendResultMessage(state, player)
    }

  private def sendCurrentStateToSingle(receiver: Option[Player]): Unit = {
    val s = sender()
    implicit val timeout: Timeout = Timeout(200, TimeUnit.MILLISECONDS)
    implicit val executionContext: ExecutionContextExecutor = context.dispatcher
    gameFieldActor.ask(GameFieldControllerActor.GetGrid).mapTo[GameFieldControllerActor.GetGridAck].foreach {
      case GameFieldControllerActor.GetGridAck(state) =>
        s ! GameControllerMessages.GameUpdated(state)
        receiver.filter(state.isCurrentPlayer).foreach { _ => s ! GameControllerMessages.YourTurn(state) }
    }
  }

  private def handleSelectPosition(player: Player, pos: GridPosition): Unit = {
    val s = sender()
    implicit val timeout: Timeout = Timeout(200, TimeUnit.MILLISECONDS)
    implicit val executionContext: ExecutionContextExecutor = context.dispatcher
    gameFieldActor.ask(GameFieldControllerActor.SelectPosition(player, pos)).mapTo[SelectPositionAck].foreach {
      case GameFieldControllerActor.SelectPositionAck(state, retCode) => self ! PositionSelected(player, pos, state, retCode, s)
    }
  }

  override def receive: Receive = {
    case GameControllerMessages.SetPos(pos) if playerListCircle.contains(sender()) => handleSelectPosition(Player.Circle, pos)
    case GameControllerMessages.SetPos(pos) if playerListCross.contains(sender()) => handleSelectPosition(Player.Cross, pos)

    case PositionSelected(player, pos, state, retCode, sender) => handleSelectPosAck(retCode, state, pos, player, sender)

    case GameControllerMessages.RegisterObserver =>
      observerList.add(sender())
      sendCurrentStateToSingle(None)

    case GameControllerMessages.RegisterCircle =>
      observerList.add(sender())
      playerListCircle.add(sender())
      sendCurrentStateToSingle(Some(Player.Circle))

    case GameControllerMessages.RegisterCross =>
      observerList.add(sender())
      playerListCross.add(sender())
      sendCurrentStateToSingle(Some(Player.Cross))

    case GameControllerMessages.Unregister =>
      observerList.remove(sender())
      playerListCircle.remove(sender())
      playerListCross.remove(sender())
  }
}
