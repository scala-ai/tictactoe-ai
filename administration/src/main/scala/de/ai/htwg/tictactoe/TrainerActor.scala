package de.ai.htwg.tictactoe

import scala.util.Random

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import de.ai.htwg.tictactoe.TrainerActor.StartTraining
import de.ai.htwg.tictactoe.aiClient.AiActor
import de.ai.htwg.tictactoe.aiClient.AiActor.LearningProcessorConfiguration
import de.ai.htwg.tictactoe.aiClient.AiActor.RegisterGame
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStepConfiguration
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMainActor
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor
import de.ai.htwg.tictactoe.logicClient.LogicPlayerActor
import de.ai.htwg.tictactoe.playerClient.PlayerUiActor
import grizzled.slf4j.Logging

object TrainerActor {
  def props() = Props(new TrainerActor())

  case class StartTraining(count: Int)
}

class TrainerActor extends Actor with Logging {

  private val dimensions = 4
  private val epsGreedyConfiguration = EpsGreedyConfiguration(
    minEpsilon = 0.3f,
    nbEpochVisits = 10000,
    random = Random
  )
  private val explorationStepConfiguration = ExplorationStepConfiguration(
    minEpsilon = 0.2f,
    nbStepVisits = 100,
    random = Random
  )
  private val properties = LearningProcessorConfiguration(
    explorationStepConfiguration,
    QLearningConfiguration(
      alpha = 0.8,
      gamma = 0.4
    )
  )
  private val watcher = context.actorOf(WatcherActor.props())
  private val cross = context.actorOf(AiActor.props(List(self, watcher), properties))
  private val circle = context.actorOf(LogicPlayerActor.props(new Random(5L)))
  private val clientMain = context.actorOf(UiMainActor.props(dimensions), "clientMain")
  private val aiClients = 1 // count of players to wait for ready message

  private var readyActors: List[ActorRef] = List()
  private var sequence = 0
  private var remainingEpochs = 0
  private var currentGame: ActorRef = _

  override def receive: Receive = {
    case StartTraining(epochs) if epochs > 0 =>
      remainingEpochs = epochs
      debug(s"Start training with $epochs remaining epochs")
      val gameName = "game" + epochs
      val game = context.actorOf(GameControllerActor.props(dimensions, Player.Cross), gameName)
      cross ! AiActor.RegisterGame(Player.Cross, game)
      circle ! LogicPlayerActor.RegisterGame(Player.Circle, game)
      currentGame = game

    case AiActor.TrainingFinished if remainingEpochs == 1 =>
      debug(s"Start test run after training")
      sequence += 1
      val gameName = "test-game" + sequence
      val game = context.actorOf(GameControllerActor.props(dimensions, Player.Cross), gameName)
      sender() ! RegisterGame(Player.Circle, game)
      context.actorOf(PlayerUiActor.props(Player.Cross, clientMain, game, gameName))

    case AiActor.TrainingFinished =>
      readyActors = sender() :: readyActors
      debug(s"training finished message (ready = ${readyActors.size})")
      currentGame ! PoisonPill
      if (readyActors.size == aiClients) {
        readyActors = List()
        self ! StartTraining(remainingEpochs - 1)
      }
  }

}
