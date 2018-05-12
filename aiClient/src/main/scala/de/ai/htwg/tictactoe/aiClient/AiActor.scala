package de.ai.htwg.tictactoe.aiClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Stash
import de.ai.htwg.tictactoe.aiClient.AiActor.LearningProcessorConfiguration
import de.ai.htwg.tictactoe.aiClient.AiActor.SaveState
import de.ai.htwg.tictactoe.aiClient.AiActor.TrainingEpochResult
import de.ai.htwg.tictactoe.aiClient.AiActor.UpdateTrainingState
import de.ai.htwg.tictactoe.aiClient.learning.TTTEpochResult
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.TTTState
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.PolicyConfiguration
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerReady
import de.ai.htwg.tictactoe.clientConnection.messages.RegisterGame
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.util.DelegatedPartialFunction
import grizzled.slf4j.Logging


object AiActor {
  def props(dimensions: Int, trainingId: String) =
    Props(new AiActor(List(), LearningProcessorConfiguration(dimensions, EpsGreedyConfiguration(), QLearningConfiguration()), trainingId))

  def props(watchers: List[ActorRef], properties: LearningProcessorConfiguration, trainingId: String) =
    Props(new AiActor(watchers, properties, trainingId))

  case object SaveState
  case class TrainingEpochResult(result: GameControllerMessages.GameResult)
  case class UpdateTrainingState(training: Boolean)

  case class LearningProcessorConfiguration(
      dimensions: Int,
      policyProperties: PolicyConfiguration,
      qLearningProperties: QLearningConfiguration,
  )
}

class AiActor private(watchers: List[ActorRef], properties: LearningProcessorConfiguration, trainingId: String) extends Actor with Stash with Logging {

  override def receive: Receive = new PreInitialized

  private class PreInitialized(
      private var learningUnit: Option[TTTLearningProcessor] = None,
      private var training: Boolean = true
  ) extends DelegatedPartialFunction[Any, Unit] {
    case class InitNet(net: TTTLearningProcessor)

    var register: Option[RegisterGame] = None

    if (learningUnit.isEmpty) {
      implicit val disp: ExecutionContextExecutor = context.dispatcher
      Future {
        // long, blocking initialisation calls in actors can lead to problems.
        // execute in another thread and send to self.
        val net = TTTLearningProcessor(
          properties.dimensions,
          policyProperties = properties.policyProperties,
          qLearningProperties = properties.qLearningProperties,
        )
        self ! InitNet(net)
      }
    }

    override def pf: Receive = {
      case InitNet(n) =>
        learningUnit = Some(n)
        probeInit()
      case r @ RegisterGame(_, _) =>
        register = Some(r)
        probeInit()
      case UpdateTrainingState(b) => training = b

      case _ => stash()
    }

    def probeInit(): Unit = for {
      n <- learningUnit
      r <- register
    } {
      context.become(new Initialized(n, r.player, r.gameControllerActor, training))
      unstashAll()
    }
  }

  private class Initialized(
      private var learningUnit: TTTLearningProcessor,
      private val currentPlayer: Player,
      private val gameActor: ActorRef,
      private var training: Boolean
  ) extends DelegatedPartialFunction[Any, Unit] {

    currentPlayer match {
      case Player.Circle => gameActor ! GameControllerMessages.RegisterCircle
      case Player.Cross => gameActor ! GameControllerMessages.RegisterCross
    }

    override def pf: Receive = {
      case GameControllerMessages.GameUpdated(_) => trace("game updated") // not interesting
      case GameControllerMessages.GameFinished(_, _) => trace("game finished") // not interesting
      case GameControllerMessages.PosAlreadySet(_: GridPosition) => error(s"$currentPlayer: Pos already set")
      case GameControllerMessages.NotYourTurn(_: GridPosition) => error(s"$currentPlayer: Not your turn")
      case GameControllerMessages.YourTurn(gf: GameField) => doGameAction(gf, sender())
      case UpdateTrainingState(b) => training = b
      case GameControllerMessages.YourResult(_, result) =>
        debug(s"AiPlayer: Game finished, result: $result")
        val epochResult = result match {
          case GameControllerMessages.GameWon => TTTEpochResult.won
          case GameControllerMessages.GameLost => TTTEpochResult.lost
          case GameControllerMessages.GameDraw => TTTEpochResult.undecided
        }
        watchers.foreach(_ ! TrainingEpochResult(result))
        debug(s"AiPlayer: Ready to play")
        watchers.foreach(_ ! PlayerReady)
        learningUnit = learningUnit.trainResult(epochResult)
        context.become(new PreInitialized(Some(learningUnit), training))
      case SaveState => learningUnit.persist(trainingId)
    }

    private def doGameAction(gf: GameField, gameControllerActor: ActorRef): Unit = {
      trace(s"$currentPlayer: It is your turn")
      // if actor is in training state calculate a training decision, else calculate best action
      val (action, newLearningUnit) = if (training) learningUnit.getTrainingDecision(TTTState(gf)) else learningUnit.getBestDecision(TTTState(gf))
      learningUnit = newLearningUnit
      gameControllerActor ! GameControllerMessages.SetPos(action.coordinate)
    }
  }
}
