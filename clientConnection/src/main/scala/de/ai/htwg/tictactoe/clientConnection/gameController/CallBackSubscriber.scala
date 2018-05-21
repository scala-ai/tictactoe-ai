package de.ai.htwg.tictactoe.clientConnection.gameController

import de.ai.htwg.tictactoe.clientConnection.gameController.GameController.Result.GameFinished
import de.ai.htwg.tictactoe.clientConnection.gameController.GameController.Result.GameUpdated
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.Player

class CallBackSubscriber private(callBack: (GameField, Option[Player]) => Unit) extends GameControllerSubscriber {
  override def notify(pub: GameController, event: GameController.Updates): Unit = event match {
    case GameUpdated(_) => // Nothing
    case GameFinished(field, winner) => callBack(field, winner)
  }
}
object CallBackSubscriber {
  def apply(callBack: (GameField, Option[Player]) => Unit) = new CallBackSubscriber(callBack)
  def apply(callBack: Option[Player] => Unit) = new CallBackSubscriber((_, w) => callBack(w))
}
