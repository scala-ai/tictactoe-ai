package de.ai.htwg.tictactoe

import akka.actor.ActorSystem
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMainActor
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy3xBuilder
import grizzled.slf4j.Logging

object TrainMain extends App with Logging {
  // this direct logger call will prevent calls to not initialized loggers in a multi threaded environment (actor system)
  trace("game start")
  val system = ActorSystem()
  //  val strategy = TTTWinStrategy4xBuilder
  val strategy = TTTWinStrategy3xBuilder
  val clientMain = system.actorOf(UiMainActor.props(strategy.dimensions), "clientMain")
  val trainer = system.actorOf(TrainerActor.props(strategy, clientMain), "trainerActor")
  trainer ! TrainerActor.StartTraining(10000)

}
