package de.ai.htwg.tictactoe

import akka.actor.ActorSystem

object TrainMain extends App {
  val system = ActorSystem()

  val game = system.actorOf(TrainerActor.props(system), "trainerActor")
  game ! TrainerActor.StartTraining(10)
}
