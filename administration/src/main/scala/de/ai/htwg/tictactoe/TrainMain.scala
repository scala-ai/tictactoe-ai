package de.ai.htwg.tictactoe

import akka.actor.ActorSystem
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMainActor
import grizzled.slf4j.Logging

object TrainMain extends App with Logging {
  // this direct logger call will prevent calls to not initialized loggers in a multi threaded environment (actor system)
  trace("game start")
  val system = ActorSystem()
  val dimensions = 4
  val clientMain = system.actorOf(UiMainActor.props(dimensions), "clientMain")
  val trainer = system.actorOf(TrainerActor.props(dimensions, clientMain), "trainerActor")
  trainer ! TrainerActor.StartTraining(10000)

}
