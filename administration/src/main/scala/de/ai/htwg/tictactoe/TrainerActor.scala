package de.ai.htwg.tictactoe

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import de.ai.htwg.tictactoe.TrainerActor.StartTraining
import de.ai.htwg.tictactoe.aiClient.AiActor
import de.ai.htwg.tictactoe.clientConnection.fxUI.ClientMainActor
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerActor
import grizzled.slf4j.Logging

object TrainerActor {
  def props(system: ActorSystem) = Props(new TrainerActor(system))

  case class StartTraining(count: Int)
}

class TrainerActor private(system: ActorSystem)
  extends Actor with Logging {

  val dimensions = 4

  override def receive: Receive = {
    case StartTraining(count) => {
      info("Start training with" + count)
      // TODO listen for game win and start next one
      // TODO reuse ai actors to create training effect
      val gameName = "game"
      val clientMain = system.actorOf(ClientMainActor.props(dimensions), "clientMain")
      val game = system.actorOf(GameControllerActor.props(dimensions, Player.Cross), gameName)
      val circle = system.actorOf(AiActor.props(Player.Circle, clientMain, game, gameName))
      val cross = system.actorOf(AiActor.props(Player.Circle, clientMain, game, gameName))
    }
  }

}
