package de.ai.htwg.tictactoe

import akka.actor.Actor
import akka.actor.Props
import de.ai.htwg.tictactoe.TrainerActor.StartTraining
import de.ai.htwg.tictactoe.aiClient.AiActor
import de.ai.htwg.tictactoe.aiClient.AiActor.RegisterGame
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages
import de.ai.htwg.tictactoe.clientConnection.messages.GameControllerMessages.GameFinished
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor
import grizzled.slf4j.Logging

object TrainerActor {
  def props() = Props(new TrainerActor())

  case class StartTraining(count: Int)
}

class TrainerActor extends Actor with Logging {

  private val dimensions = 4
  private val circle = context.actorOf(AiActor.props())
  private val cross = context.actorOf(AiActor.props())

  private var remainingEpochs = 0

  override def receive: Receive = {
    case StartTraining(epochs) =>
      remainingEpochs = epochs
      info(s"Start training with $epochs remaining epochs")
      // TODO listen for game win and start next one
      val gameName = "game" + epochs
      val game = context.actorOf(GameControllerActor.props(dimensions, Player.Cross), gameName)
      circle ! RegisterGame(Player.Circle, game)
      cross ! RegisterGame(Player.Cross, game)
      // register itself as watcher TODO: use a general watch registration message
      game ! GameControllerMessages.RegisterCircle
    case GameFinished(_, _) if remainingEpochs > 0 => self ! StartTraining(remainingEpochs - 1)
  }

}
