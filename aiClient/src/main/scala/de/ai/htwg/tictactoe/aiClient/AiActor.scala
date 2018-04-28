package de.ai.htwg.tictactoe.aiClient

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.TTTState
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import grizzled.slf4j.Logging


object AiActor {
  def props(player: Player, clientMainActor: ActorRef, gameControllerActor: ActorRef, gameName: String) =
    Props(new AiActor(player, clientMainActor, gameControllerActor, gameName))
}

class AiActor private(player: Player, clientMainActor: ActorRef, gameControllerActor: ActorRef, gameName: String)
  extends Actor with Logging {
  player match {
    case Player.Circle => gameControllerActor ! GameControllerMessages.RegisterCircle
    case Player.Cross => gameControllerActor ! GameControllerMessages.RegisterCross
  }

  val learningUnit = TTTLearningProcessor()

  override def receive: Receive = {
    case GameControllerMessages.PosAlreadySet(_: GridPosition) => error("Pos already set")
    case GameControllerMessages.NotYourTurn(_: GridPosition) => error("Not your turn")
    case GameControllerMessages.PositionSet(gf: GameField) => doGameAction(gf)
    case GameControllerMessages.GameWon(winner: Player, _: GameField) =>
      debug(s"winner: $winner")
      learningUnit.trainResult(winner == player)
  }

  private def doGameAction(gf: GameField): Unit = {
    if (gf.isCurrentPlayer(player)) {
      debug("It is your turn")
      val action = learningUnit.getDecision(TTTState(gf))
      gameControllerActor ! GameControllerMessages.SetPos(action.coordinate)
    } else {
      debug("Hmm not your turn")
    }
  }
}
