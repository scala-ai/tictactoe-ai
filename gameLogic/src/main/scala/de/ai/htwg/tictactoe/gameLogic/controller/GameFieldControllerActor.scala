package de.ai.htwg.tictactoe.gameLogic.controller

import akka.actor.Actor
import akka.actor.Props
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.SelectPosition
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.RetCode.NotThisPlayersTurn
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.RetCode.PositionAlreadySelected
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.RetCode.GameAlreadyFinished

object GameFieldControllerActor {


  def props(startingPlayer: Player) = Props(new GameFieldControllerActor(startingPlayer))

  case object GetGrid
  case class GetGridAck(gameField: GameField)
  case class SelectPosition(p: Player, pos: GridPosition)

  case class SelectPositionAck(p: Player, pos: GridPosition, state: GameField, returnCode: RetCode)

  sealed trait RetCode
  object RetCode {
    case object PositionSet extends RetCode
    case object GameWon extends RetCode
    case object GameAlreadyFinished extends RetCode
    case object PositionAlreadySelected extends RetCode
    case object NotThisPlayersTurn extends RetCode
  }

}

class GameFieldControllerActor private(startingPlayer: Player) extends Actor {
  var gameField: GameField = GameField(startingPlayer)
  private val comp = GameFieldControllerActor
  override def receive: Receive = {

    case GameFieldControllerActor.GetGrid => sender ! GameFieldControllerActor.GetGridAck(gameField)

    case SelectPosition(p, pos) if !gameField.isCurrentPlayer(p) => sender ! comp.SelectPositionAck(p, pos, gameField, NotThisPlayersTurn)
    case SelectPosition(p, pos) if gameField.posIsSet(pos) => sender ! comp.SelectPositionAck(p, pos, gameField, PositionAlreadySelected)
    case SelectPosition(p, pos) if gameField.isFinished => sender ! comp.SelectPositionAck(p, pos, gameField, GameAlreadyFinished)
    case SelectPosition(p, pos) =>
      gameField = gameField.setPos(pos)
      val ret = if (gameField.isFinished) {
        comp.RetCode.GameWon
      } else {
        comp.RetCode.PositionSet
      }

      sender ! comp.SelectPositionAck(p, pos, gameField, ret)
  }

}



