package de.ai.htwg.tictactoe

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration

import akka.actor.ActorSystem

object TrainMain extends App {
  val system = ActorSystem()

  val trainer = system.actorOf(TrainerActor.props(), "trainerActor")

  // TODO this delay is necessary, cause the actor needs some time to init its neural network
  // also fixes the logger not initialized problem
  private implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  system.scheduler.scheduleOnce(Duration(4, TimeUnit.SECONDS), trainer, TrainerActor.StartTraining(10000))
}
