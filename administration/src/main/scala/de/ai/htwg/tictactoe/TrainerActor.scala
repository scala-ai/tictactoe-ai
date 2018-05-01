package de.ai.htwg.tictactoe

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import de.ai.htwg.tictactoe.TrainerActor.StartTraining
import de.ai.htwg.tictactoe.aiClient.AiActor
import de.ai.htwg.tictactoe.aiClient.AiActor.RegisterGame
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMainActor
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor
import de.ai.htwg.tictactoe.playerClient.PlayerUiActor
import grizzled.slf4j.Logging

object TrainerActor {
  def props() = Props(new TrainerActor())

  case class StartTraining(count: Int)
}

class TrainerActor extends Actor with Logging {

  private val dimensions = 4
  private val circle = context.actorOf(AiActor.props(List(self)))
  private val cross = context.actorOf(AiActor.props(List(self)))
  private val clientMain = context.actorOf(UiMainActor.props(dimensions), "clientMain")

  private var readyActors: List[ActorRef] = List()

  private var sequence = 0

  private var remainingEpochs = 0

  override def receive: Receive = {
    case StartTraining(epochs) if epochs > 0 =>
      remainingEpochs = epochs
      info(s"Start training with $epochs remaining epochs")
      val gameName = "game" + epochs
      val game = context.actorOf(GameControllerActor.props(dimensions, Player.Cross), gameName)
      circle ! RegisterGame(Player.Circle, game)
      cross ! RegisterGame(Player.Cross, game)

    case AiActor.TrainingFinished if remainingEpochs == 0 =>
      info(s"Start test run after training")
      sequence += 1
      val gameName = "test-game" + sequence
      val game = context.actorOf(GameControllerActor.props(dimensions, Player.Cross), gameName)
      circle ! RegisterGame(Player.Circle, game)
      val realPlayer = context.actorOf(PlayerUiActor.props(Player.Cross, clientMain, game, gameName))

    case AiActor.TrainingFinished => {
      readyActors = sender() :: readyActors
      debug(s"training finished message (ready = ${readyActors.size})")
      if (readyActors.size == 2) {
        readyActors = List()
        self ! StartTraining(remainingEpochs - 1)
      }
    }
  }

}
