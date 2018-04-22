package de.ai.htwg.tictactoe.gameLogic.controller

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor.SetPos
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor.RegisterCircle
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor.RegisterCross
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor.Unregister
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.SelectPositionAck

object GameControllerActor {

  def props() = Props(new GameControllerActor())


  case object RegisterCross
  case object RegisterCircle
  case object Unregister

  case class SetPos(pos: GridPosition)
  case class PosAlreadySet(pos: GridPosition)
  case class NotYourTurn(pos: GridPosition)
  case class GameWon(winner: Player, gf: GameField)
  case class PositionSet(gf: GameField)
}


class GameControllerActor private() extends Actor {
  private val gameFieldActor: ActorRef = context.actorOf(GameFieldControllerActor.props(Player.Circle))
  private var playerListCircle: List[ActorRef] = Nil
  private var playerListCross: List[ActorRef] = Nil

  private var winner: Option[Player] = None

  private val RetCode = GameFieldControllerActor.RetCode

  implicit class ActorListExtension(list: List[ActorRef]) {
    def !!(msg: Any) = list.foreach { r => r ! msg }
  }

  private def handleGameAlreadyWon(state: GameField, players: List[ActorRef]): Unit = {
    winner.foreach { w => players !! GameControllerActor.GameWon(w, state) }
  }


  private def handleSelectPosAck(retCode: GameFieldControllerActor.RetCode, state: GameField, pos: GridPosition, player: Player): Unit = {
    (player, retCode) match {
      case (_, RetCode.PositionSet) =>
        playerListCircle !! GameControllerActor.PositionSet(state)
        playerListCross !! GameControllerActor.PositionSet(state)

      case (_, RetCode.GameWon) =>
        winner = Some(player)
        playerListCircle !! GameControllerActor.GameWon(player, state)
        playerListCross !! GameControllerActor.GameWon(player, state)

      case (Player.Circle, RetCode.GameAlreadyFinished) => handleGameAlreadyWon(state, playerListCircle)
      case (Player.Cross, RetCode.GameAlreadyFinished) => handleGameAlreadyWon(state, playerListCross)

      case (Player.Circle, RetCode.NotThisPlayersTurn) => playerListCircle !! GameControllerActor.NotYourTurn(pos)
      case (Player.Cross, RetCode.NotThisPlayersTurn) => playerListCross !! GameControllerActor.NotYourTurn(pos)
    }
  }

  override def receive: Receive = {
    case SetPos(pos) if playerListCircle.contains(sender()) => gameFieldActor ! GameFieldControllerActor.SelectPosition(Player.Circle, pos)
    case SetPos(pos) if playerListCross.contains(sender()) => gameFieldActor ! GameFieldControllerActor.SelectPosition(Player.Cross, pos)
    case SelectPositionAck(player, pos, state, retCode) => handleSelectPosAck(retCode, state, pos, player)
    case RegisterCircle => playerListCircle = sender() :: playerListCircle
    case RegisterCross => playerListCross = sender() :: playerListCross
    case Unregister =>
      playerListCircle = playerListCircle.filterNot(_ == sender())
      playerListCross = playerListCross.filterNot(_ == sender())

  }
}
