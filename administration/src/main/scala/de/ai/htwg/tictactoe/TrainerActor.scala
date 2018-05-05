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
import de.ai.htwg.tictactoe.aiClient.AiActor.RegisterGame
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStepConfiguration
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.util.DelegatedPartialFunction
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor
import de.ai.htwg.tictactoe.playerClient.PlayerUiActor
import grizzled.slf4j.Logging

object TrainerActor {
  def props(dimensions: Int, clientMain: ActorRef) = Props(new TrainerActor(dimensions, clientMain))

  case class StartTraining(count: Int)
}

class TrainerActor(dimensions: Int, clientMain: ActorRef) extends Actor with Stash with Logging {
  private type DelegateReceive = DelegatedPartialFunction[Any, Unit]

  private val epsGreedyConfiguration = EpsGreedyConfiguration(
    minEpsilon = 0.3f,
    nbEpochVisits = 10000,
    random = Random
  )
  private val explorationStepConfiguration = ExplorationStepConfiguration(
    minEpsilon = 0.2f,
    nbStepVisits = 400,
    random = Random
  )
  private val properties = LearningProcessorConfiguration(
    explorationStepConfiguration,
    QLearningConfiguration(
      alpha = 0.8,
      gamma = 0.4
    )
  )

  private val watcherActor = context.actorOf(WatcherActor.props())

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
    var currentGame: ActorRef = doTraining(
      context.actorOf(AiActor.props(List(self, watcherActor), properties)),
      context.actorOf(AiActor.props(List(self), properties)),
    )

    def doTraining(circle: ActorRef, cross: ActorRef): ActorRef = {
      info(s"Train epoch ${totalEpochs - remainingEpochs}")
      val game = context.actorOf(GameControllerActor.props(dimensions, Player.Cross), s"game-$remainingEpochs")
      circle ! RegisterGame(Player.Circle, game)
      cross ! RegisterGame(Player.Cross, game)
      game
    }

    override def pf: PartialFunction[Any, Unit] = {
      case AiActor.TrainingFinished =>
        debug(s"training finished message (ready = ${readyActors.size})")
        readyActors match {
          case Nil =>
            readyActors = sender() :: Nil

          case first :: Nil =>
            remainingEpochs -= 1
            if (remainingEpochs > 0) {
              readyActors = Nil
              currentGame ! PoisonPill // FIXME turn into restart
              // this will mix who is circle and who is cross
              currentGame = doTraining(sender(), first)
            } else {
              readyActors = sender() :: first :: Nil
              context.become(new RunTestGames(readyActors.toVector))
              warn {
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
      val game = context.actorOf(GameControllerActor.props(dimensions, Player.Cross), gameName)
      val p = if (random.nextBoolean()) Player.Cross else Player.Circle

      info(s"Start test run. player is: $p")

      val player = context.actorOf(PlayerUiActor.props(p, clientMain, game, gameName))
      ai ! RegisterGame(Player.other(p), game)
      CurrentPlayerGame(player, game)
    }

    override def pf: PartialFunction[Any, Unit] = {
      case AiActor.TrainingFinished =>
        // FIXME turn into restart
        state.game ! PoisonPill
        state.player ! PlayerUiActor.Euthanize
        testGameNumber += 1
        state = runTestGame()
    }
  }

}
