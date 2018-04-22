package de.ai.htwg.tictactoe.aiClient

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.state.TTTState
import de.ai.htwg.tictactoe.clientConnection.fxUI.ClientMainActor
import de.ai.htwg.tictactoe.clientConnection.fxUI.GameUiActor
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
  clientMainActor ! ClientMainActor.CreateGameUI(s"$gameName-$player")

  class MainState(uiActor: ActorRef) extends Receive {
    override def isDefinedAt(x: Any): Boolean = pf.isDefinedAt(x)
    override def apply(v1: Any): Unit = pf.apply(v1)

    val learningUnit = TTTLearningProcessor()

    val pf: Receive = {
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
        uiActor ! GameUiActor.PrintField(gf)
      case GameControllerMessages.GameWon(winner: Player, gf: GameField) =>
        println(s"winner: $winner")
        uiActor ! GameUiActor.PrintField(gf)
        learningUnit.trainResult(winner == player)
    }
  }

  def receivePreStart: Receive = {
    case ClientMainActor.ReturnGameUI(ref) => context.become(new MainState(ref))
  }

  override def receive: Receive = receivePreStart
}
