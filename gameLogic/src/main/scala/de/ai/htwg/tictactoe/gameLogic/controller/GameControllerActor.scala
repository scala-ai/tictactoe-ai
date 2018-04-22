package de.ai.htwg.tictactoe.gameLogic.controller

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
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

  private def handleGameAlreadyWon(state: GameField, players: List[ActorRef]): Unit = {
    winner.foreach { w => players !! GameControllerMessages.GameWon(w, state) }
  }

  private def handleSelectPosAck(retCode: GameFieldControllerActor.RetCode, state: GameField, pos: GridPosition, player: Player): Unit = {
    (player, retCode) match {
      case (_, RetCode.PositionSet) =>
        playerListCircle !! GameControllerMessages.PositionSet(state)
        playerListCross !! GameControllerMessages.PositionSet(state)

      case (_, RetCode.GameWon) =>
        winner = Some(player)
        playerListCircle !! GameControllerMessages.GameWon(player, state)
        playerListCross !! GameControllerMessages.GameWon(player, state)

      case (Player.Circle, RetCode.GameAlreadyFinished) => handleGameAlreadyWon(state, playerListCircle)
      case (Player.Cross, RetCode.GameAlreadyFinished) => handleGameAlreadyWon(state, playerListCross)

      case (Player.Circle, RetCode.NotThisPlayersTurn) => playerListCircle !! GameControllerMessages.NotYourTurn(pos)
      case (Player.Cross, RetCode.NotThisPlayersTurn) => playerListCross !! GameControllerMessages.NotYourTurn(pos)
    }
  }

  override def receive: Receive = {
    case GameControllerMessages.SetPos(pos) if playerListCircle.contains(sender()) => gameFieldActor ! GameFieldControllerActor.SelectPosition(Player.Circle, pos)
    case GameControllerMessages.SetPos(pos) if playerListCross.contains(sender()) => gameFieldActor ! GameFieldControllerActor.SelectPosition(Player.Cross, pos)
    case GameFieldControllerActor.SelectPositionAck(player, pos, state, retCode) => handleSelectPosAck(retCode, state, pos, player)
    case GameControllerMessages.RegisterCircle => playerListCircle = sender() :: playerListCircle
    case GameControllerMessages.RegisterCross => playerListCross = sender() :: playerListCross
    case GameControllerMessages.Unregister =>
      playerListCircle = playerListCircle.filterNot(_ == sender())
      playerListCross = playerListCross.filterNot(_ == sender())

  }
}
