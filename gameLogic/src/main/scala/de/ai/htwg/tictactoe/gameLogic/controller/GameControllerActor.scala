package de.ai.htwg.tictactoe.gameLogic.controller

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.ask
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player

object GameControllerActor {
  def props(dimensions: Int, startingPlayer: Player) = Props(new GameControllerActor(dimensions, startingPlayer))
}

class GameControllerActor private(dimensions: Int, startingPlayer: Player) extends Actor {
  private val gameFieldActor: ActorRef = context.actorOf(GameFieldControllerActor.props(startingPlayer = startingPlayer, dimensions))
  private var playerListCircle: List[ActorRef] = Nil
  private var playerListCross: List[ActorRef] = Nil

  private var winner: Option[Player] = None

  private val RetCode = GameFieldControllerActor.RetCode

  implicit class ActorListExtension(list: List[ActorRef]) {
    def !!(msg: Any): Unit = list.foreach { r => r ! msg }
  }

  private def handleGameAlreadyFinished(state: GameField, players: List[ActorRef], receiver: Player): Unit = {
    // TODO handle Game Draw
    winner.foreach {
      case `receiver` => players !! GameControllerMessages.GameFinished(GameControllerMessages.GameWon, state)
      case _ => players !! GameControllerMessages.GameFinished(GameControllerMessages.GameLost, state)
    }
  }

  private def handleSelectPosAck(retCode: GameFieldControllerActor.RetCode, state: GameField, pos: GridPosition, player: Player): Unit = {
    (player, retCode) match {
      case (_, RetCode.PositionAlreadySelected) => ??? // TODO Nicolas
      case (_, RetCode.PositionSet) =>
        playerListCircle !! GameControllerMessages.PositionSet(state)
        playerListCross !! GameControllerMessages.PositionSet(state)

      case (_, RetCode.GameWon) =>
        winner = Some(player)
        handleGameAlreadyFinished(state, playerListCircle, Player.Circle)
        handleGameAlreadyFinished(state, playerListCross, Player.Cross)

      case (Player.Circle, RetCode.GameAlreadyFinished) => handleGameAlreadyFinished(state, playerListCircle, Player.Circle)
      case (Player.Cross, RetCode.GameAlreadyFinished) => handleGameAlreadyFinished(state, playerListCross, Player.Cross)

      case (Player.Circle, RetCode.NotThisPlayersTurn) => playerListCircle !! GameControllerMessages.NotYourTurn(pos)
      case (Player.Cross, RetCode.NotThisPlayersTurn) => playerListCross !! GameControllerMessages.NotYourTurn(pos)
    }
  }

  private def sendCurrentState(receiver: Player): Unit = {
    val s = sender()
    gameFieldActor.ask(GameFieldControllerActor.GetGrid).mapTo[GameFieldControllerActor.GetGridAck].foreach {
      case GameFieldControllerActor.GetGridAck(state) =>
        s ! GameControllerMessages.PositionSet(state)
        if (state.isCurrentPlayer(receiver)) s ! GameControllerMessages.YourTurn
    }
  }

  override def receive: Receive = {
    case GameControllerMessages.SetPos(pos) if playerListCircle.contains(sender()) => gameFieldActor ! GameFieldControllerActor.SelectPosition(Player.Circle, pos)
    case GameControllerMessages.SetPos(pos) if playerListCross.contains(sender()) => gameFieldActor ! GameFieldControllerActor.SelectPosition(Player.Cross, pos)
    case GameFieldControllerActor.SelectPositionAck(player, pos, state, retCode) => handleSelectPosAck(retCode, state, pos, player)

    case GameControllerMessages.RegisterCircle =>
      playerListCircle = sender() :: playerListCircle
      sendCurrentState(Player.Circle)

    case GameControllerMessages.RegisterCross =>
      playerListCross = sender() :: playerListCross
      sendCurrentState(Player.Cross)

    case GameControllerMessages.Unregister =>
      playerListCircle = playerListCircle.filterNot(_ == sender())
      playerListCross = playerListCross.filterNot(_ == sender())

  }
}
