package de.ai.htwg.tictactoe

import java.util.Timer
import java.util.TimerTask

import akka.actor.ActorSystem

object TrainMain extends App {
  val system = ActorSystem()

  val trainer = system.actorOf(TrainerActor.props(), "trainerActor")

  val timer = new Timer

  private def delay(f: () => Unit, n: Long): Unit = timer.schedule(new TimerTask() {
    def run(): Unit = f()
  }, n)

  // TODO this delay is necessary, cause the actor needs some time to init its neural network
  // also fixes the logger not initialized problem
  delay(() => trainer ! TrainerActor.StartTraining(3), 4000)
}
