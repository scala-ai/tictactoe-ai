package de.ai.htwg.tictactoe.gameLogic.controller

import akka.actor.Actor
import akka.actor.Props
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPositionOLD
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.SelectPosition
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.RetCode.GameAlreadyFinished
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.RetCode.NotThisPlayersTurn
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.RetCode.PositionAlreadySelected

object GameFieldControllerActor {


  def props(startingPlayer: Player, dimensions: Int) = Props(new GameFieldControllerActor(startingPlayer, dimensions))

  case object GetGrid
  case class GetGridAck(gameField: GameField)
  case class SelectPosition(p: Player, pos: GridPositionOLD)

  case class SelectPositionAck(state: GameField, returnCode: RetCode)

  sealed trait RetCode
  object RetCode {
    case object PositionSet extends RetCode
    case object GameWon extends RetCode
    case object GameUndecided extends RetCode
    case object GameAlreadyFinished extends RetCode
    case object PositionAlreadySelected extends RetCode
    case object NotThisPlayersTurn extends RetCode
  }

}

class GameFieldControllerActor private(startingPlayer: Player, dimensions: Int) extends Actor {
  var gameField: GameField = GameField(startingPlayer, dimensions)
  private val comp = GameFieldControllerActor

  override def receive: Receive = {

    case GameFieldControllerActor.GetGrid => sender ! GameFieldControllerActor.GetGridAck(gameField)

    case SelectPosition(p, pos) if !gameField.isCurrentPlayer(p) => sender ! comp.SelectPositionAck(gameField, NotThisPlayersTurn)
    case SelectPosition(p, pos) if gameField.posIsSet(pos) => sender ! comp.SelectPositionAck(gameField, PositionAlreadySelected)
    case SelectPosition(p, pos) if gameField.isFinished => sender ! comp.SelectPositionAck(gameField, GameAlreadyFinished)
    case SelectPosition(p, pos) =>
      gameField = gameField.setPos(pos)
      val ret = if (gameField.isFinished) {
        if (gameField.finishedUndecided) {
          comp.RetCode.GameUndecided
        } else {
          comp.RetCode.GameWon
        }
      } else {
        comp.RetCode.PositionSet
      }

      sender ! comp.SelectPositionAck(gameField, ret)
  }

}



