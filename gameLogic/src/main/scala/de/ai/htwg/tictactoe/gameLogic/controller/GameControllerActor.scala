package de.ai.htwg.tictactoe.gameLogic.controller

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Stash
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.util.DelegatedPartialFunction

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

  override def receive: Receive = new Initialized(GameField(startingPlayer, dimensions))

  private class Initialized(
      var state: GameField,
  ) extends DelegatedPartialFunction[Any, Unit] {
    unstashAll()
    private val observerList: SubscriberList = new SubscriberList()
    private val playerListCircle: PlayerList = new PlayerList(Player.Circle)
    private val playerListCross: PlayerList = new PlayerList(Player.Cross)
    private val gameFieldController = new GameFieldController(TTTWinStrategy4xBuilder)

    override def pf: PartialFunction[Any, Unit] = {
      case GameControllerMessages.SetPos(pos) if playerListCircle.contains(sender()) => handleSelectPosition(Player.Circle, pos)
      case GameControllerMessages.SetPos(pos) if playerListCross.contains(sender()) => handleSelectPosition(Player.Cross, pos)

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
      //      gameFieldActor ! PoisonPill
      context.become(new GameFinished(winner, state))
    }

    private def handleSelectPosition(player: Player, pos: GridPosition): Unit = {
      val RES = GameFieldController.Result
      gameFieldController.setPos(pos, player) match {
        case RES.GameUpdated(field) =>
          state = field
          observerList !! GameControllerMessages.GameUpdated(state)
          playerToList(Player.other(player)) !! GameControllerMessages.YourTurn(state)
        case RES.GameFinished(field, winner) => handleFinishGame(winner, field)
        case RES.NotThisPlayersTurn(_, _) => sender ! GameControllerMessages.NotYourTurn(pos)
        case RES.PositionAlreadySelected(_, _) => sender ! GameControllerMessages.PosAlreadySet(pos)
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
