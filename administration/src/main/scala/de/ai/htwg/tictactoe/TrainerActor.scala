package de.ai.htwg.tictactoe

import scala.util.Random

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Stash
import de.ai.htwg.tictactoe.TrainerActor.StartTraining
import de.ai.htwg.tictactoe.aiClient.AiActor
import de.ai.htwg.tictactoe.aiClient.AiActor.LearningProcessorConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStepConfiguration
import de.ai.htwg.tictactoe.clientConnection.messages.PlayerReady
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategyBuilder
import de.ai.htwg.tictactoe.clientConnection.util.DelegatedPartialFunction
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController
import de.ai.htwg.tictactoe.gameLogic.messages
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
    minEpsilon = 0.5f,
    nbEpochVisits = 50000,
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
      alpha = 0.03,
      gamma = 0.3
    )
  )
  private val watcherActor = context.actorOf(WatcherActor.props(trainingId), "watcherActor")
  private val aiActor = context.actorOf(AiActor.props(List(self), properties, trainingId), "aiPlayer")
  private val randomPlayer = context.actorOf(RandomPlayerActor.props(random, List(self)), "randomPlayer")
  private val logicPlayer = context.actorOf(LogicPlayerActor.props(strategyBuilder, random, List(self)), "logicPlayer")

  override def receive: Receive = PreInitialize

  private object PreInitialize extends DelegateReceive {
    override def pf: PartialFunction[Any, Unit] = {
      case StartTraining(epochs) =>
        if (epochs < 0) {
          error(s"cannot train less than 1 epoch: $epochs")
          throw new IllegalStateException(s"cannot train less than 1 epoch: $epochs")
        }
        start = System.currentTimeMillis()
        info(s"Start training $trainingId with $epochs epochs")
        context.become(new Training(epochs))
        unstashAll()

      case _ => stash()
    }
  }

  private class Training(val totalEpochs: Int) extends DelegateReceive {
    private var remainingEpochs: Int = totalEpochs
    private var readyActors: List[ActorRef] = Nil

    var currentGame: GameFieldController = doTraining(
      aiActor,
      randomPlayer
    )

    def doTraining(circle: ActorRef, cross: ActorRef): GameFieldController = {
      info(s"Train epoch $remainingEpochs")
      val gameFieldController = new GameFieldController(strategyBuilder, Player.Cross)

      circle ! messages.RegisterGame(Player.Circle, gameFieldController, training = true)
      cross ! messages.RegisterGame(Player.Cross, gameFieldController, training = true)
      gameFieldController
    }

    override def pf: PartialFunction[Any, Unit] = {
      case PlayerReady(_) => handlePlayerReady(sender())
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
            // this will mix who is circle and who is cross
            if (remainingEpochs % 10000 == 0) {
              first ! AiActor.SaveState
              sender ! AiActor.SaveState
              watcherActor ! WatcherActor.PrintCSV
            }
            if (remainingEpochs % 5 == 0) {
//            if (remainingEpochs % 200 == 0) {
              context.become(new RunTestGames(aiActor, remainingEpochs))
            } else {
              currentGame = doTraining(sender, first)
            }
          } else {
            readyActors = sender :: first :: Nil
            first ! AiActor.SaveState
            sender ! AiActor.SaveState
            context.become(new RunUiGames(aiActor))
            watcherActor ! WatcherActor.PrintCSV
            info {
              val time = System.currentTimeMillis() - start
              val ms = time % 1000
              val secs = time / 1000 % 60
              val min = time / 1000 / 60
              s"Training $trainingId finished after $min min $secs sec $ms ms."
            }
          }
      }
    }
  }

  private class RunTestGames(val ai: ActorRef, val epochs: Int) extends DelegateReceive {
    info(s"Running test games, epoch: $epochs")
    private var testGameNumber = 100
    private var readyActors: List[ActorRef] = Nil
    private var gameController: GameFieldController = runTestGame()
    private var wonGames = 0
    private var lostGames = 0
    private var drawGames = 0

    def runTestGame(): GameFieldController = {
      readyActors = Nil
      val startingPlayer = if (random.nextBoolean()) Player.Cross else Player.Circle
      val gameFieldController = new GameFieldController(strategyBuilder, startingPlayer)
      debug(s"Start test run: $epochs - $testGameNumber")
      ai ! messages.RegisterGame(Player.Circle, gameFieldController, training = false)
      logicPlayer ! messages.RegisterGame(Player.Cross, gameFieldController, training = false)
      gameFieldController
    }

    override def pf: PartialFunction[Any, Unit] = {
      case PlayerReady(winner) => readyActors match {
        case Nil =>
          readyActors = sender() :: Nil
        case _ :: _ =>
          readyActors = sender() :: readyActors
          handleGameFinish(winner)
      }
    }

    private def handleGameFinish(winner: Option[Player]): Unit = {
      winner match {
        case Some(Player.Cross) => wonGames += 1
        case Some(Player.Circle) => lostGames += 1
        case None => drawGames += 1
      }
      testGameNumber -= 1
      if (testGameNumber < 0) {
        info(f"$epochs: + $wonGames  - $lostGames  o $drawGames => ${(wonGames + drawGames).toFloat * 100 / (wonGames + lostGames + drawGames)}%.2f%%")
        watcherActor ! WatcherActor.EpochResult(epochs, wonGames, lostGames, drawGames)
        context.become(new Training(epochs - 1))
      } else {
        gameController = runTestGame()
      }
    }
  }

  private class RunUiGames(val ai: ActorRef) extends DelegateReceive {
    trace("now in state RunUiGames")
    case class CurrentPlayerGame(player: ActorRef, game: GameFieldController)
    var testGameNumber = 0
    var state: CurrentPlayerGame = runUiGame()
    private var readyActors: List[ActorRef] = Nil


    def runUiGame(): CurrentPlayerGame = {
      readyActors = Nil
      val gameName = s"testGame-$testGameNumber"
      info(s"run testGame: $gameName")
      val gameFieldController = new GameFieldController(strategyBuilder, Player.Cross)

      // update actor state to non training
      val startingPlayer = if (random.nextBoolean()) Player.Cross else Player.Circle
      val player = context.actorOf(PlayerUiActor.props(startingPlayer, clientMain, gameFieldController, gameName, self))
      ai ! messages.RegisterGame(Player.Cross, gameFieldController, training = false)
      CurrentPlayerGame(player, gameFieldController)
    }

    override def pf: PartialFunction[Any, Unit] = {
      case PlayerReady(winner) => readyActors match {
        case Nil =>
          readyActors = sender() :: Nil
        case _ :: _ =>
          readyActors = sender() :: readyActors
          info("start next testGame")
          handleGameFinish(winner)
      }
    }
    private def handleGameFinish(winner: Option[Player]): Unit = {
      info {
        winner match {
          case Some(Player.Cross) => s"AI-Player wins"
          case Some(Player.Circle) => s"Human-Player wins"
          case None => "No winner in this game"
        }
      }

      // FIXME turn into restart
      state.player ! PlayerUiActor.Euthanize
      testGameNumber += 1
      state = runUiGame()
    }
  }

}
