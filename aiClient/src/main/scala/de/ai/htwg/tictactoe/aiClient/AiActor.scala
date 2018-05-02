package de.ai.htwg.tictactoe.aiClient

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import de.ai.htwg.tictactoe.aiClient.AiActor.TrainingFinished
import de.ai.htwg.tictactoe.aiClient.learning.TTTEpochResult
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.TTTState
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import grizzled.slf4j.Logging


object AiActor {
  def props() = Props(new AiActor(List()))

  def props(watchers: List[ActorRef]) = Props(new AiActor(watchers))

  case class RegisterGame(player: Player, gameControllerActor: ActorRef)
  case object TrainingFinished
}

class AiActor private(watchers: List[ActorRef]) extends Actor with Logging {

  // TODO remove this
  private var currentPlayer: Player = Player.Cross

  private var learningUnit = TTTLearningProcessor()

  override def receive: Receive = {
    case AiActor.RegisterGame(p, game) => handleSetGame(p, game)

    case GameControllerMessages.GameUpdated(_) => trace("game updated") // not interesting
    case GameControllerMessages.GameFinished(_, _) => trace("game finished") // not interesting
    case GameControllerMessages.PosAlreadySet(_: GridPosition) => error(s"$currentPlayer: Pos already set")
    case GameControllerMessages.NotYourTurn(_: GridPosition) => error(s"$currentPlayer: Not your turn")
    case GameControllerMessages.YourTurn(gf: GameField) => doGameAction(gf, sender())

    case GameControllerMessages.YourResult(_, result) =>
      debug(s"$currentPlayer: game finished, result: $result")
      learningUnit = learningUnit.trainResult(result match {
        case GameControllerMessages.GameWon => TTTEpochResult.won
        case GameControllerMessages.GameLost => TTTEpochResult.lost
        case GameControllerMessages.GameDraw => TTTEpochResult.undecided
      })
      watchers.foreach(_ ! TrainingFinished)
  }

  private def handleSetGame(player: Player, gameControllerActor: ActorRef): Unit = {
    currentPlayer = player
    player match {
      case Player.Circle => gameControllerActor ! GameControllerMessages.RegisterCircle
      case Player.Cross => gameControllerActor ! GameControllerMessages.RegisterCross
    }
    info(s"$currentPlayer: ai player is ready to play")
  }

  private def doGameAction(gf: GameField, gameControllerActor: ActorRef): Unit = {
    trace(s"$currentPlayer: It is your turn")
    val (action, newLearningUnit) = learningUnit.getDecision(TTTState(gf))
    learningUnit = newLearningUnit
    gameControllerActor ! GameControllerMessages.SetPos(action.coordinate)
  }
}
