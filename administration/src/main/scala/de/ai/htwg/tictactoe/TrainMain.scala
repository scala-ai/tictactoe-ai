package de.ai.htwg.tictactoe

import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMain
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy3xBuilder
import grizzled.slf4j.Logging

object TrainMain extends App with Logging {
  // this direct logger call will prevent calls to not initialized loggers in a multi threaded environment (actor system)
  trace("game start")
  //  val strategy = TTTWinStrategy4xBuilder
  val strategy = TTTWinStrategy3xBuilder
  val clientMain = UiMain(strategy.dimensions)
  val trainer = new Trainer(strategy, clientMain)
  trainer.startTraining(10000)

}
