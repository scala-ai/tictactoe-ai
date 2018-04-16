package de.htwg.controller

import akka.actor.Actor
import akka.actor.Props
import de.htwg.model.SelectPosition
import de.htwg.model.Player
import de.htwg.model.GameField
import de.htwg.model.SelectPositionAck
import de.htwg.model.SelectPositionAck.SelectPositionReturnCode.NotThisPlayersTurn
import de.htwg.model.SelectPositionAck.SelectPositionReturnCode.PositionAlreadySelected
import de.htwg.model.SelectPositionAck.SelectPositionReturnCode.GameAlreadyFinished
import de.htwg.model.SelectPositionAck.SelectPositionReturnCode.GameWon
import de.htwg.model.SelectPositionAck.SelectPositionReturnCode.PositionSet

class GameControllerActor private(startingPlayer: Player) extends Actor {
  var gameField: GameField = GameField(startingPlayer)

  override def receive: Receive = {

    case GameControllerActor.GetGrid => sender ! GameControllerActor.GetGridAck(gameField)

    case SelectPosition(p, pos) if !gameField.isCurrentPlayer(p) => sender ! SelectPositionAck(p, pos, gameField, NotThisPlayersTurn)
    case SelectPosition(p, pos) if gameField.posIsSet(pos) => sender ! SelectPositionAck(p, pos, gameField, PositionAlreadySelected)
    case SelectPosition(p, pos) if gameField.isFinished => sender ! SelectPositionAck(p, pos, gameField, GameAlreadyFinished)
    case SelectPosition(p, pos) =>
      gameField = gameField.setPos(pos)
      val ret = if (gameField.isFinished) {
        GameWon
      } else {
        PositionSet
      }

      sender ! SelectPositionAck(p, pos, gameField, ret)
  }

}


object GameControllerActor {
  case object GetGrid
  case class GetGridAck(gameField: GameField)

  def props(startingPlayer: Player) = Props(new GameControllerActor(startingPlayer))
}
