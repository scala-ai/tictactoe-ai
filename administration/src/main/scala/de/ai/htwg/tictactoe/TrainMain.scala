package de.ai.htwg.tictactoe

import akka.actor.ActorSystem
import grizzled.slf4j.Logging

object TrainMain extends App with Logging {
  // this direct logger call will prevent calls to not initialized loggers in a multi threaded environment (actor system)
  trace("game start")
  val system = ActorSystem()

  val trainer = system.actorOf(TrainerActor.props(), "trainerActor")
  trainer ! TrainerActor.StartTraining(100000)

}
