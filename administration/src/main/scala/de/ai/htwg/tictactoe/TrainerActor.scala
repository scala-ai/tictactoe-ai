package de.ai.htwg.tictactoe

import scala.util.Random

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.Stash
import de.ai.htwg.tictactoe.TrainerActor.StartTraining
import de.ai.htwg.tictactoe.aiClient.AiActor
import de.ai.htwg.tictactoe.aiClient.AiActor.LearningProcessorConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStepConfiguration
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.messages.RegisterGame
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.util.DelegatedPartialFunction
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor
import de.ai.htwg.tictactoe.logicClient.LogicPlayerActor
import de.ai.htwg.tictactoe.playerClient.PlayerUiActor
import grizzled.slf4j.Logging

object TrainerActor {
  def props(dimensions: Int, clientMain: ActorRef) = Props(new TrainerActor(dimensions, clientMain))

  case class StartTraining(count: Int)
}

class TrainerActor(dimensions: Int, clientMain: ActorRef) extends Actor with Stash with Logging {
  // every totalSteps / saveStateQuotient steps, the model is persisted
  private val saveStateQuotient = 100

  private type DelegateReceive = DelegatedPartialFunction[Any, Unit]

  private val epsGreedyConfiguration = EpsGreedyConfiguration(
    minEpsilon = 0.75f,
    nbEpochVisits = 4000000,
    random = Random
  )
  private val explorationStepConfiguration = ExplorationStepConfiguration(
    minEpsilon = 0.2f,
    nbStepVisits = 1000,
    random = Random
  )
  private val properties = LearningProcessorConfiguration(
    epsGreedyConfiguration,
    QLearningConfiguration(
      alpha = 0.8,
      gamma = 0.6
    )
  )

  override def receive: Receive = PreInitialize

  private object PreInitialize extends DelegateReceive {
    override def pf: PartialFunction[Any, Unit] = {
      case StartTraining(epochs) =>
        if (epochs < 0) {
          error(s"cannot train vor less than 1 epoch: $epochs")
          throw new IllegalStateException(s"cannot train vor less than 1 epoch: $epochs")
        }

        info(s"Start training with $epochs epochs")
        context.become(new Training(epochs))
        unstashAll()

      case _ => stash()
    }
  }

  private class Training(val totalEpochs: Int) extends DelegateReceive {
    val start: Long = System.currentTimeMillis()
    var remainingEpochs: Int = totalEpochs
    private var readyActors: List[ActorRef] = Nil
    private val watcherActor = context.actorOf(WatcherActor.props())

    var currentGame: ActorRef = doTraining(
      context.actorOf(AiActor.props(List(self, watcherActor), properties)),
      context.actorOf(LogicPlayerActor.props(new Random(5L), List(self)))
      // context.actorOf(AiActor.props(List(self), properties))
    )

    def doTraining(circle: ActorRef, cross: ActorRef): ActorRef = {
      info(s"Train epoch ${totalEpochs - remainingEpochs}")
      val game = context.actorOf(GameControllerActor.props(dimensions, Player.Cross), s"game-$remainingEpochs")
      // must be sent twice, cause we don't know the player type
      circle ! AiActor.UpdateTrainingState(true)
      cross ! AiActor.UpdateTrainingState(true)

      circle ! RegisterGame(Player.Circle, game)
      cross ! RegisterGame(Player.Cross, game)
      game
    }

    override def pf: PartialFunction[Any, Unit] = {
      case AiActor.TrainingFinished => handlePlayerReady(sender())
      case LogicPlayerActor.PlayerReady => handlePlayerReady(sender())
    }

    private def handlePlayerReady(sender: ActorRef): Unit = {
      debug(s"training finished message (ready = ${readyActors.size})")
      readyActors match {
        case Nil =>
          readyActors = sender :: Nil

        case first :: _ =>
          remainingEpochs -= 1
          if (remainingEpochs > 0) {
            readyActors = Nil
            currentGame ! PoisonPill // FIXME turn into restart
            // this will mix who is circle and who is cross
            currentGame = doTraining(sender, first)
            if (remainingEpochs % (totalEpochs / saveStateQuotient) == 0) {
              first ! AiActor.SaveState
              sender ! AiActor.SaveState
            }
          } else {
            readyActors = sender :: first :: Nil
            first ! AiActor.SaveState
            sender ! AiActor.SaveState
            context.become(new RunTestGames(readyActors.toVector))
            watcherActor ! WatcherActor.PrintCSV(100)
            info {
              val time = System.currentTimeMillis() - start
              val ms = time % 1000
              val secs = time / 1000 % 60
              val min = time / 1000 / 60
              s"Training of $totalEpochs epochs finished after $min min $secs sec $ms ms."
            }
          }
      }
    }
  }

  private class RunTestGames(val trainedActors: Vector[ActorRef]) extends DelegateReceive {
    case class CurrentPlayerGame(player: ActorRef, game: ActorRef)
    var testGameNumber = 0
    val gameName = s"testGame - $testGameNumber"
    val random = new Random()
    var state: CurrentPlayerGame = runTestGame()

    def runTestGame(): CurrentPlayerGame = {
      info(s"Start test run. ")
      val gameName = s"testGame-$testGameNumber"
      val ai = trainedActors(random.nextInt(trainedActors.size))
      // update actor state to non training
      ai ! AiActor.UpdateTrainingState(false)
      val game = context.actorOf(GameControllerActor.props(dimensions, Player.Cross), gameName)
      game ! GameControllerMessages.RegisterObserver
      val p = if (random.nextBoolean()) Player.Cross else Player.Circle

      info(s"Start test run. player is: $p")

      val player = context.actorOf(PlayerUiActor.props(p, clientMain, game, gameName))
      ai ! RegisterGame(Player.other(p), game)
      CurrentPlayerGame(player, game)
    }

    override def pf: PartialFunction[Any, Unit] = {
      case GameControllerMessages.GameFinished(_, winner) =>
        info {
          winner match {
            case Some(player) => s"winner: $player"
            case None => "game drawn"
          }
        }
        // FIXME turn into restart
        state.game ! PoisonPill
        state.player ! PlayerUiActor.Euthanize
        testGameNumber += 1
        state = runTestGame()
    }
  }

}
