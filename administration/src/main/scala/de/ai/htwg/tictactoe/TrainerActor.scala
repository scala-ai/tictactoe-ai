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
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerReady
import de.ai.htwg.tictactoe.clientConnection.messages.RegisterGame
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategyBuilder
import de.ai.htwg.tictactoe.clientConnection.util.DelegatedPartialFunction
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor
import de.ai.htwg.tictactoe.logicClient.LogicPlayerActor
import de.ai.htwg.tictactoe.logicClient.RandomPlayerActor
import de.ai.htwg.tictactoe.playerClient.PlayerUiActor
import grizzled.slf4j.Logging

object TrainerActor {
  def props(strategyBuilder: TTTWinStrategyBuilder, clientMain: ActorRef) = Props(new TrainerActor(strategyBuilder, clientMain))

  case class StartTraining(count: Int)
}

class TrainerActor(strategyBuilder: TTTWinStrategyBuilder, clientMain: ActorRef) extends Actor with Stash with Logging {
  private type DelegateReceive = DelegatedPartialFunction[Any, Unit]
  private val random = new Random(1L)
  private var start = 0L
  // unique training id for a whole training execution run
  private val trainingId = Random.alphanumeric.take(6).mkString

  private val epsGreedyConfiguration = EpsGreedyConfiguration(
    minEpsilon = 0.75f,
    nbEpochVisits = 4000000,
    random = random
  )
  private val explorationStepConfiguration = ExplorationStepConfiguration(
    minEpsilon = 0.2f,
    nbStepVisits = 1000,
    random = random
  )
  private val properties = LearningProcessorConfiguration(
    strategyBuilder.dimensions,
    epsGreedyConfiguration,
    QLearningConfiguration(
      alpha = 0.1,
      gamma = 0.5
    )
  )
  private val watcherActor = context.actorOf(WatcherActor.props(trainingId))
  private val aiActor = context.actorOf(AiActor.props(List(self), properties, trainingId))
  private val randomPlayer = context.actorOf(RandomPlayerActor.props(random, List(self)))
  private val logicPlayer = context.actorOf(LogicPlayerActor.props(strategyBuilder, random, List(self)))

  override def receive: Receive = PreInitialize

  private object PreInitialize extends DelegateReceive {
    override def pf: PartialFunction[Any, Unit] = {
      case StartTraining(epochs) =>
        if (epochs < 0) {
          error(s"cannot train less than 1 epoch: $epochs")
          throw new IllegalStateException(s"cannot train less than 1 epoch: $epochs")
        }
        start = System.currentTimeMillis()
        info(s"Start training with $epochs epochs")
        context.become(new Training(epochs))
        unstashAll()

      case _ => stash()
    }
  }

  private class Training(val totalEpochs: Int) extends DelegateReceive {
    private var remainingEpochs: Int = totalEpochs
    private var readyActors: List[ActorRef] = Nil

    var currentGame: ActorRef = doTraining(
      aiActor,
      randomPlayer
    )

    def doTraining(circle: ActorRef, cross: ActorRef): ActorRef = {
      debug(s"Train epoch $remainingEpochs")
      val game = context.actorOf(GameControllerActor.props(Player.Cross, strategyBuilder), s"game-$remainingEpochs")
      // must be sent twice, cause we don't know the player type
      circle ! AiActor.UpdateTrainingState(true)
      cross ! AiActor.UpdateTrainingState(true)

      circle ! RegisterGame(Player.Circle, game)
      cross ! RegisterGame(Player.Cross, game)
      game
    }

    override def pf: PartialFunction[Any, Unit] = {
      case PlayerReady => handlePlayerReady(sender())
    }

    private def handlePlayerReady(sender: ActorRef): Unit = {
      trace(s"training finished message (ready = ${readyActors.size})")
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
            if (remainingEpochs % 10000 == 0) {
              first ! AiActor.SaveState
              sender ! AiActor.SaveState
              watcherActor ! WatcherActor.PrintCSV
            }
            if (remainingEpochs % 200 == 0) {
              context.become(new RunTestGames(Vector(aiActor), remainingEpochs))
            }
          } else {
            readyActors = sender :: first :: Nil
            first ! AiActor.SaveState
            sender ! AiActor.SaveState
            context.become(new RunUiGames(Vector(aiActor)))
            watcherActor ! WatcherActor.PrintCSV
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

  private class RunTestGames(val trainedActors: Vector[ActorRef], val epochs: Int) extends DelegateReceive {
    case class CurrentPlayerGame(game: ActorRef)
    private var testGameNumber = 100
    private var readyActors: List[ActorRef] = Nil
    private var state: CurrentPlayerGame = runTestGame()
    private var wonGames = 0
    private var lostGames = 0
    private var drawGames = 0

    def runTestGame(): CurrentPlayerGame = {
      val gameName = s"testGame-$epochs-$testGameNumber"
      val ai = trainedActors(random.nextInt(trainedActors.size))
      // update actor state to non training
      ai ! AiActor.UpdateTrainingState(false)
      val p = if (random.nextBoolean()) Player.Cross else Player.Circle
      val game = context.actorOf(GameControllerActor.props(p, strategyBuilder), gameName)
      game ! GameControllerMessages.RegisterObserver
      debug(s"Start test run: $epochs - $testGameNumber")
      ai ! RegisterGame(Player.Cross, game)
      logicPlayer ! RegisterGame(Player.Circle, game)
      CurrentPlayerGame(game)
    }

    override def pf: PartialFunction[Any, Unit] = {
      case PlayerReady => readyActors match {
        case Nil =>
          readyActors = sender() :: Nil
        case _ :: _ =>
          readyActors = sender() :: readyActors
          handleGameFinish(sender())
      }
      case GameControllerMessages.GameFinished(_, winner) =>
        winner match {
          case Some(Player.Cross) => wonGames += 1
          case Some(Player.Circle) => lostGames += 1
          case None => drawGames += 1
        }
        sender() ! PoisonPill
    }

    private def handleGameFinish(sender: ActorRef): Unit = {
      testGameNumber -= 1
      if (testGameNumber < 0) {
        info(s"$epochs: + $wonGames  - $lostGames  o $drawGames => ${(wonGames + drawGames).toFloat / (wonGames + lostGames + drawGames)} %")
        watcherActor ! WatcherActor.EpochResult(epochs, wonGames, lostGames, drawGames)
        context.become(new Training(epochs - 1))
      } else {
        state = runTestGame()
      }
    }
  }

  private class RunUiGames(val trainedActors: Vector[ActorRef]) extends DelegateReceive {
    case class CurrentPlayerGame(player: ActorRef, game: ActorRef)
    var testGameNumber = 0
    var state: CurrentPlayerGame = runUiGame()

    def runUiGame(): CurrentPlayerGame = {
      val gameName = s"testGame-$testGameNumber"
      val ai = trainedActors(random.nextInt(trainedActors.size))
      // update actor state to non training
      ai ! AiActor.UpdateTrainingState(false)
      val p = if (random.nextBoolean()) Player.Cross else Player.Circle
      val game = context.actorOf(GameControllerActor.props(p, strategyBuilder), gameName)
      game ! GameControllerMessages.RegisterObserver
      val player = context.actorOf(PlayerUiActor.props(Player.Circle, clientMain, game, gameName))
      ai ! RegisterGame(Player.Cross, game)
      CurrentPlayerGame(player, game)
    }

    override def pf: PartialFunction[Any, Unit] = {
      case GameControllerMessages.GameFinished(_, winner) =>
        info {
          winner match {
            case Some(Player.Cross) => s"AI-Player wins"
            case Some(Player.Circle) => s"Human-Player wins"
            case None => "No winner in this game"
          }
        }
        // FIXME turn into restart
        state.game ! PoisonPill
        state.player ! PlayerUiActor.Euthanize
        testGameNumber += 1
        state = runUiGame()
    }
  }

}
