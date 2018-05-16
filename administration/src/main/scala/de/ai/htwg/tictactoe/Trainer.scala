package de.ai.htwg.tictactoe

import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.AiLearning
import de.ai.htwg.tictactoe.aiClient.AiLearning.LearningProcessorConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.QLearningConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.EpsGreedyConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.ExplorationStepConfiguration
import de.ai.htwg.tictactoe.aiClient.learning.core.policy.PolicyConfiguration
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMain
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategyBuilder
import de.ai.htwg.tictactoe.clientConnection.util.SingleThreadPlatform
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldController
import de.ai.htwg.tictactoe.logicClient.LogicPlayer
import de.ai.htwg.tictactoe.logicClient.RandomPlayer
import de.ai.htwg.tictactoe.playerClient.UiPlayer
import grizzled.slf4j.Logging

object Trainer {
  val saveFrequency = 10000
  val testFrequency = 200
  val runsPerTest = 100

  def buildEpsGreedyConfiguration(random: Random): PolicyConfiguration = EpsGreedyConfiguration(
    minEpsilon = 0.5f,
    nbEpochVisits = 50000,
    random = random
  )

  def buildExplorationStepConfiguration(random: Random): PolicyConfiguration = ExplorationStepConfiguration(
    minEpsilon = 0.2f,
    nbStepVisits = 1000,
    random = random
  )

}

class Trainer(strategyBuilder: TTTWinStrategyBuilder, clientMain: UiMain) extends Logging {
  private val seed = new Random().nextLong
  info(s"Chosen seed for this run: $seed")
  private val random = new Random(seed)
  // unique training id for a whole training execution run
  private val trainingId = Random.alphanumeric.take(6).mkString

  private val properties = LearningProcessorConfiguration(
    strategyBuilder.dimensions,
    Trainer.buildEpsGreedyConfiguration(random),
    QLearningConfiguration(
      alpha = 0.03,
      gamma = 0.3
    )
  )

  private val possibleWinActions: List[TTTWinStrategy] = strategyBuilder.listAllWinStrategies

  private val watcher = new Watcher(trainingId, seed)
  private val aiTrainer = new AiLearning(properties, trainingId)
  val platform: SingleThreadPlatform = SingleThreadPlatform()

  def startTraining(epochs: Int): Unit = {
    if (epochs < 0) {
      error(s"cannot train less than 1 epoch: $epochs")
      throw new IllegalStateException(s"cannot train less than 1 epoch: $epochs")
    }
    val start = System.currentTimeMillis()
    info(s"Start training $trainingId with $epochs epochs")
    doAllTraining(epochs, epochs, () => doAfterTraining(start))
  }

  def doAfterTraining(start: Long): Unit = {
    aiTrainer.saveState()
    watcher.printCSV()
    info {
      val time = System.currentTimeMillis() - start
      val ms = time % 1000
      val secs = time / 1000 % 60
      val min = time / 1000 / 60
      s"Training $trainingId finished after $min min $secs sec $ms ms."
    }
    runUiGame(0)
  }

  private def doAllTraining(totalEpochs: Int, remainingEpochs: Int, doAfter: () => Unit): Unit = {
    debug(s"Train epoch $remainingEpochs")
    if (remainingEpochs < 0) {
      platform.execute(doAfter())
      return
    }


    if (remainingEpochs % Trainer.saveFrequency == 0 && remainingEpochs != totalEpochs) {
      aiTrainer.saveState()
      watcher.printCSV()
    }

    var readyPlayer = 0

    def handlePlayerReady(): Unit = {
      trace(s"training finished message (ready = $readyPlayer)")
      readyPlayer += 1
      if (readyPlayer == 2) {
        if (remainingEpochs % Trainer.testFrequency == 0 && remainingEpochs != totalEpochs) {
          runTestGame(Trainer.runsPerTest, TestGameData(remainingEpochs, 0, 0, 0, 0), () => doAllTraining(totalEpochs, remainingEpochs - 1, doAfter))
        } else {
          platform.execute {
            doAllTraining(totalEpochs, remainingEpochs - 1, doAfter)
          }
        }
      }
    }

    val startPlayer = if (random.nextBoolean()) Player.Cross else Player.Circle
    val gameFieldController = new GameFieldController(strategyBuilder, startPlayer)

    aiTrainer.registerGame(gameFieldController, training = true, _ => handlePlayerReady())
    val randomPlayer = new RandomPlayer(Player.Circle, random, _ => handlePlayerReady())
    gameFieldController.subscribe(randomPlayer)
  }


  private case class TestGameData(
      remainingEpochs: Int,
      wonGames: Int,
      lostGames: Int,
      drawGamesOffense: Int,
      drawGamesDefense: Int,
  )

  private def runTestGame(testGameNumber: Int, data: TestGameData, doAfter: () => Unit): Unit = {
    debug(s"Start test run: ${data.remainingEpochs} - $testGameNumber")
    var readyPlayers = 0
    val startPlayer = if (testGameNumber % 2 == 0) Player.Cross else Player.Circle

    def handleGameFinish(winner: Option[Player]): Unit = {
      readyPlayers += 1
      if (readyPlayers >= 2) {
        val newData = winner match {
          case Some(Player.Cross) => data.copy(wonGames = data.wonGames + 1)
          case Some(Player.Circle) => data.copy(lostGames = data.lostGames + 1)
          case None => if (startPlayer == Player.Cross) {
            data.copy(drawGamesOffense = data.drawGamesOffense + 1)
          } else {
            data.copy(drawGamesDefense = data.drawGamesDefense + 1)
          }
        }
        platform.execute {
          runTestGame(testGameNumber - 1, newData, doAfter)
        }
      }
    }

    if (testGameNumber <= 0) {
      val epochs = data.remainingEpochs
      val wonGames = data.wonGames
      val lostGames = data.lostGames
      val drawGames = data.drawGamesOffense + data.drawGamesDefense
      info(f"$epochs: + $wonGames  - $lostGames  o $drawGames => ${(wonGames + data.drawGamesDefense).toFloat * 100 / (wonGames + lostGames + drawGames)}%.2f%%")
      watcher.addEpochResult(Watcher.EpochResult(epochs, wonGames, lostGames, data.drawGamesOffense, data.drawGamesDefense))
      platform.execute {
        doAfter()
      }
    } else {
      val gameFieldController = new GameFieldController(strategyBuilder, startPlayer)

      aiTrainer.registerGame(gameFieldController, training = false, handleGameFinish)
      val logicPlayer = new LogicPlayer(Player.Circle, random, possibleWinActions, handleGameFinish)
      gameFieldController.subscribe(logicPlayer)
    }
  }

  def runUiGame(testGameNumber: Int): Unit = {
    var readyPlayers = 0
    val startPlayer = if (random.nextBoolean()) Player.Cross else Player.Circle
    val gameFieldController = new GameFieldController(strategyBuilder, startPlayer)
    val gameName = s"testGame-$testGameNumber"
    info(s"run testGame: $gameName")

    def handleGameFinish(winner: Option[Player]): Unit = {
      readyPlayers += 1
      if (readyPlayers >= 2) {
        info {
          winner match {
            case Some(Player.Circle) => s"Human-Player wins"
            case Some(Player.Cross) => s"AI-Player wins"
            case None => "No winner in this game"
          }
        }
        runUiGame(testGameNumber + 1)
      }
    }

    clientMain.getNewStage(gameName).foreach { gameUi =>
      val playerUi = new UiPlayer(Player.Circle, gameUi, gameFieldController.getGrid(), platform, handleGameFinish)
      gameFieldController.subscribe(playerUi)
      aiTrainer.registerGame(gameFieldController, training = false, handleGameFinish)
    }(platform.executionContext)
  }
}
