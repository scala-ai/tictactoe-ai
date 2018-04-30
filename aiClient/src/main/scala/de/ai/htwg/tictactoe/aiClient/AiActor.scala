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
  def props() =
    Props(new AiActor())

  case class RegisterGame(player: Player, gameControllerActor: ActorRef)
}

class AiActor private() extends Actor with Logging {

  // TODO remove this
  private var currentPlayer: Player = Player.Cross

  private val learningUnit = TTTLearningProcessor()

  override def receive: Receive = {
    case GameControllerMessages.PosAlreadySet(_: GridPosition) => error("Pos already set")
    case GameControllerMessages.NotYourTurn(_: GridPosition) => error("Not your turn")
    case GameControllerMessages.PositionSet(gf: GameField) => doGameAction(gf, sender())
    case GameControllerMessages.GameWon(winner: Player, _: GameField) =>
      debug(s"winner: $winner")
      learningUnit.trainResult(winner == currentPlayer)
    case AiActor.RegisterGame(p, game) => handleSetGame(p, game)
  }

  private def handleSetGame(player: Player, gameControllerActor: ActorRef): Unit = {
    currentPlayer = player
    player match {
      case Player.Circle => gameControllerActor ! GameControllerMessages.RegisterCircle
      case Player.Cross => gameControllerActor ! GameControllerMessages.RegisterCross
    }
    info("ai player is ready to play")
  }

  private def doGameAction(gf: GameField, gameControllerActor: ActorRef): Unit = {
    if (gf.isCurrentPlayer(currentPlayer)) {
      trace("It is your turn")
      val action = learningUnit.getDecision(TTTState(gf))
      gameControllerActor ! GameControllerMessages.SetPos(action.coordinate)
    } else {
      trace("Hmm not your turn")
    }
  }
}
