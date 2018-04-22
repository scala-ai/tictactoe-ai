package de.ai.htwg.tictactoe.aiClient

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.state.TTTState
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player


object AiActor {
  def props(player: Player, clientMainActor: ActorRef, gameControllerActor: ActorRef, gameName: String) =
    Props(new AiActor(player, clientMainActor, gameControllerActor, gameName))
}

class AiActor private(player: Player, clientMainActor: ActorRef, gameControllerActor: ActorRef, gameName: String) extends Actor {
  player match {
    case Player.Circle => gameControllerActor ! GameControllerMessages.RegisterCircle
    case Player.Cross => gameControllerActor ! GameControllerMessages.RegisterCross
  }

  val learningUnit = TTTLearningProcessor()

  override def receive: Receive = {
    // TODO remove all println
    case GameControllerMessages.PosAlreadySet(_: GridPosition) => println("Pos already set")
    case GameControllerMessages.NotYourTurn(_: GridPosition) => println("Not your turn")
    case GameControllerMessages.PositionSet(gf: GameField) =>
      if (gf.isCurrentPlayer(player)) {
        println("It is your turn")
        val action = learningUnit.getDecision(TTTState(gf))
        gameControllerActor ! GameControllerMessages.SetPos(action.coordinate)
      } else {
        println("Hmm not your turn")
      }
    case GameControllerMessages.GameWon(winner: Player, _: GameField) =>
      println(s"winner: $winner")
      learningUnit.trainResult(winner == player)
  }
}
