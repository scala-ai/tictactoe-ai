package de.ai.htwg.tictactoe.gameLogic.controller

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContextExecutor

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Stash
import akka.actor.PoisonPill
import akka.pattern.ask
import akka.util.Timeout
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.util.DelegatedPartialFunction
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.SelectPositionAck
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.RetCode

object GameControllerActor {
  def props(dimensions: Int, startingPlayer: Player) = Props(new GameControllerActor(dimensions, startingPlayer))
}

class GameControllerActor private(dimensions: Int, startingPlayer: Player) extends Actor with Stash {

  private class SubscriberList(var list: List[ActorRef] = Nil) {
    def !!(msg: Any): Unit = list.foreach { r => r ! msg }
    def add(subscriber: ActorRef): Unit = list = subscriber :: list.filterNot(_ == subscriber)
    def remove(subscriber: ActorRef): Unit = list = list.filterNot(_ == subscriber)
    def contains(subscriber: ActorRef): Boolean = list.contains(subscriber)
  }
  private class PlayerList(val player: Player) extends SubscriberList

  private case class PositionSelected(p: Player, pos: GridPosition, state: GameField, returnCode: RetCode, sender: ActorRef)

  private val RetCode = GameFieldControllerActor.RetCode

  override def receive: Receive = new PreInitialized()

  private class PreInitialized extends DelegatedPartialFunction[Any, Unit] {
    private val gameFieldActor: ActorRef = context.actorOf(GameFieldControllerActor.props(startingPlayer = startingPlayer, dimensions))
    gameFieldActor ! GameFieldControllerActor.GetGrid
    override def pf: PartialFunction[Any, Unit] = {
      case GameFieldControllerActor.GetGridAck(state) =>
        context.become(new Initialized(gameFieldActor, state))
      case _ => stash()
    }
  }

  private class Initialized(
      val gameFieldActor: ActorRef,
      var state: GameField,
  ) extends DelegatedPartialFunction[Any, Unit] {
    unstashAll()
    private val observerList: SubscriberList = new SubscriberList()
    private val playerListCircle: PlayerList = new PlayerList(Player.Circle)
    private val playerListCross: PlayerList = new PlayerList(Player.Cross)

    override def pf: PartialFunction[Any, Unit] = {
      case GameControllerMessages.SetPos(pos) if playerListCircle.contains(sender()) => handleSelectPosition(Player.Circle, pos)
      case GameControllerMessages.SetPos(pos) if playerListCross.contains(sender()) => handleSelectPosition(Player.Cross, pos)

      case PositionSelected(player, pos, newState, retCode, sender) =>
        state = newState
        handleSelectPosAck(retCode, pos, player, sender)

      case GameControllerMessages.RegisterObserver =>
        observerList.add(sender())
        observerList !! GameControllerMessages.GameUpdated(state)

      case GameControllerMessages.RegisterCircle =>
        registerPlayerList(playerListCircle)

      case GameControllerMessages.RegisterCross =>
        registerPlayerList(playerListCross)

      case GameControllerMessages.Unregister =>
        observerList.remove(sender())
        playerListCircle.remove(sender())
        playerListCross.remove(sender())
    }

    def registerPlayerList(pl: PlayerList): Unit = {
      observerList.add(sender())
      pl.add(sender())
      pl !! GameControllerMessages.GameUpdated(state)
      if (state.isCurrentPlayer(pl.player)) pl !! GameControllerMessages.YourTurn(state)
    }


    private def playerToList(p: Player): PlayerList = p match {
      case Player.Circle => playerListCircle
      case Player.Cross => playerListCross
    }

    private def handleFinishGame(winner: Option[Player], state: GameField): Unit = {
      observerList !! GameControllerMessages.GameFinished(state, winner)
      winner match {
        case None =>
          playerListCross !! GameControllerMessages.YourResult(state, GameControllerMessages.GameDraw)
          playerListCircle !! GameControllerMessages.YourResult(state, GameControllerMessages.GameDraw)
        case Some(win) =>
          playerToList(win) !! GameControllerMessages.YourResult(state, GameControllerMessages.GameWon)
          playerToList(Player.other(win)) !! GameControllerMessages.YourResult(state, GameControllerMessages.GameLost)
      }
      gameFieldActor ! PoisonPill
      context.become(new GameFinished(winner, state))
    }

    private def handleSelectPosAck(retCode: GameFieldControllerActor.RetCode, pos: GridPosition, player: Player, sender: ActorRef): Unit = retCode match {
      case RetCode.PositionAlreadySelected => sender ! GameControllerMessages.PosAlreadySet(pos)
      case RetCode.NotThisPlayersTurn => sender ! GameControllerMessages.NotYourTurn(pos)
      // this is an edge case that should rarely if ever happen
      case RetCode.GameAlreadyFinished => sender ! GameControllerMessages.NotYourTurn(pos)

      case RetCode.GameWon => handleFinishGame(Some(player), state)
      case RetCode.GameUndecided => handleFinishGame(None, state)

      case RetCode.PositionSet =>
        observerList !! GameControllerMessages.GameUpdated(state)
        playerToList(Player.other(player)) !! GameControllerMessages.YourTurn(state)
    }

    private def handleSelectPosition(player: Player, pos: GridPosition): Unit = {
      val s = sender()
      implicit val timeout: Timeout = Timeout(200, TimeUnit.MILLISECONDS)
      implicit val executionContext: ExecutionContextExecutor = context.dispatcher
      gameFieldActor.ask(GameFieldControllerActor.SelectPosition(player, pos)).mapTo[SelectPositionAck].foreach {
        case GameFieldControllerActor.SelectPositionAck(newState, retCode) => self ! PositionSelected(player, pos, newState, retCode, s)
      }
    }
  }

  private class GameFinished(
      val winner: Option[Player],
      var state: GameField,
  ) extends DelegatedPartialFunction[Any, Unit] {
    override def pf: PartialFunction[Any, Unit] = {
      case GameControllerMessages.SetPos(_) |
           GameControllerMessages.RegisterObserver |
           GameControllerMessages.RegisterCircle |
           GameControllerMessages.RegisterCross =>
        sender() ! GameControllerMessages.GameFinished(state, winner)
    }
  }
}
