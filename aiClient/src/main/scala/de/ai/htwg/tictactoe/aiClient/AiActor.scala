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
import de.ai.htwg.tictactoe.aiClient.learning.TTTEpochResult
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.PolicyConfiguration
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerReady
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.util.DelegatedPartialFunction
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController
import de.ai.htwg.tictactoe.gameLogic.messages.RegisterGame
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

  private[aiClient] case class StartTrainingAfterGame(learningUnit: TTTLearningProcessor, winner: Option[Player])

}

class AiActor private(watchers: List[ActorRef], properties: LearningProcessorConfiguration, trainingId: String) extends Actor with Stash with Logging {

  override def receive: Receive = new PreInitialized
  private type DelegatedReceive = DelegatedPartialFunction[Any, Unit]


  private class PreInitialized(
      private var learningUnit: Option[TTTLearningProcessor] = None,
  ) extends DelegatedReceive {
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
      case SaveState => learningUnit.foreach {
        _.persist(trainingId)
      }
      case InitNet(n) =>
        learningUnit = Some(n)
        probeInit()

      case r @ RegisterGame(_, _, _) =>
        register = Some(r)
        probeInit()

      case msg =>
        info(s"stashing message: $msg")
        stash()
    }

    def probeInit(): Unit = for {
      n <- learningUnit
      r <- register
    } {
      context.become(new Playing(n, r.player, r.gameController, r.training))
      unstashAll()
    }
  }

  private class Playing(
      learningUnit: TTTLearningProcessor,
      currentPlayer: Player,
      gameController: GameFieldController,
      training: Boolean,
  ) extends DelegatedReceive {
    val aiPlayer = new AiPlayer(learningUnit, currentPlayer, training, self)
    gameController.subscribe(aiPlayer)

    override def pf: PartialFunction[Any, Unit] = {
      case AiActor.StartTrainingAfterGame(updatedLearningUnit, winner) =>
        debug("AiActor starts training")
       val result = winner match {
        case None => GameControllerMessages.GameDraw
        case Some(`currentPlayer`) => GameControllerMessages.GameWon
        case _ /* opponent */ => GameControllerMessages.GameLost
      }

        watchers.foreach(_ ! TrainingEpochResult(result))
        val epochResult = result match {
          case GameControllerMessages.GameWon => TTTEpochResult.won
          case GameControllerMessages.GameLost => TTTEpochResult.lost
          case GameControllerMessages.GameDraw => TTTEpochResult.undecided
        }

        val trainedLearningUnit = updatedLearningUnit.trainResult(epochResult)
        debug(s"AiPlayer: Ready to play")
        watchers.foreach(_ ! PlayerReady(winner))
        context.become(new PreInitialized(Some(trainedLearningUnit)))
        unstashAll()
      case msg =>
        info(s"stashing message: $msg")
        stash()
    }
  }
}
