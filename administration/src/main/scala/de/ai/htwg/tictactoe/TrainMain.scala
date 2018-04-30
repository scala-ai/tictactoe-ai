package de.ai.htwg.tictactoe

import akka.actor.ActorSystem

object TrainMain extends App {
  val system = ActorSystem()

  val trainer = system.actorOf(TrainerActor.props(), "trainerActor")
  trainer ! TrainerActor.StartTraining(10)
}
