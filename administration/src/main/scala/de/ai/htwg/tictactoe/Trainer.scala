package de.ai.htwg.tictactoe

import scala.annotation.tailrec
import scala.util.Random

import de.ai.htwg.tictactoe.aiClient.AiLearning
import de.ai.htwg.tictactoe.aiClient.AiLearning.LearningProcessorConfiguration
import de.ai.htwg.tictactoe.clientConnection.fxUI.GameUiStage
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMain
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategyBuilder
import de.ai.htwg.tictactoe.clientConnection.util.SingleThreadPlatform
import de.ai.htwg.tictactoe.gameLogic.controller.GameControllerImpl
import de.ai.htwg.tictactoe.logicClient.LogicPlayer
import de.ai.htwg.tictactoe.logicClient.RandomPlayer
import de.ai.htwg.tictactoe.playerClient.UiPlayerController
import de.ai.htwg.tictactoe.playerClient.UiView
import grizzled.slf4j.Logging

object Trainer {
  val saveFrequency = 10000
  val testFrequency = 200
  val runsPerTest = 100
}

class Trainer(
    val trainingId: String,
    val seed: Long,
    val random: Random,
    val testSeed: Long,
    val strategyBuilder: TTTWinStrategyBuilder,
    val properties: LearningProcessorConfiguration,
    val clientMain: UiMain,
    val platform: SingleThreadPlatform
) extends Logging {
  platform.checkCurrentThread()

  info(s"Chosen seed for this run: $seed")
  private val possibleWinActions: List[TTTWinStrategy] = strategyBuilder.listAllWinStrategies

  private val watcher = new Watcher(trainingId, seed, properties)
  private val aiTrainer = AiLearning(properties, trainingId)

  def startTraining(epochs: Int): Unit = {
    if (epochs < 0) {
      error(s"cannot train less than 1 epoch: $epochs")
      throw new IllegalStateException(s"cannot train less than 1 epoch: $epochs")
    }
    val start = System.currentTimeMillis()
    info(s"Start training $trainingId with $epochs epochs")
    doAllTraining(epochs)

    aiTrainer.saveState()
    watcher.printCSV()
    info {
      val time = System.currentTimeMillis() - start
      val ms = time % 1000
      val secs = time / 1000 % 60
      val min = time / 1000 / 60
      s"Training $trainingId finished after $min min $secs sec $ms ms."
    }
    runUiGame()
  }


  private def doAllTraining(totalEpochs: Int): Unit = {

    @tailrec def trainingLoop(remainingEpochs: Int): Unit = {
      if (remainingEpochs < 0) return

      debug(s"Train epoch $remainingEpochs")

      if ((totalEpochs - remainingEpochs) % Trainer.saveFrequency == 0 && remainingEpochs != totalEpochs) {
        aiTrainer.saveState()
        watcher.printCSV()
      }

      doTrainingGame(remainingEpochs)

      if ((totalEpochs - remainingEpochs) % Trainer.testFrequency == 0 && remainingEpochs != totalEpochs) {
        runTestGame(new Random(testSeed), Trainer.runsPerTest, remainingEpochs)
      }

      trainingLoop(remainingEpochs - 1)
    }

    def doTrainingGame(remainingEpochs: Int): Unit = {
      val startPlayer = if (random.nextBoolean()) Player.Cross else Player.Circle
      val gameController = GameControllerImpl(strategyBuilder, startPlayer)

      val aiPlayer = aiTrainer.getNewAiPlayer(gameController, training = true)

      val opponent = remainingEpochs % 3 match {
        // do not train against yourself as it will produce worse results.
        // case 0 => aiTrainer.getNewAiPlayer(gameController, training = false, playerType = Player.Circle)
        case _ => new RandomPlayer(Player.Circle, random)
      }

      gameController.startGame(aiPlayer, opponent)
    }

    trainingLoop(totalEpochs)
  }


  private case class TestGameData(
      remainingEpochs: Int,
      wonGames: Int,
      lostGames: Int,
      drawGamesOffense: Int,
      drawGamesDefense: Int,
  )

  private def runTestGame(testGameRandom: Random, noOfTestGames: Int, remainingEpochs: Int): Unit = {

    @tailrec def loop(testGameNumber: Int, data: TestGameData): TestGameData = {
      if (testGameNumber > 0) {
        debug(s"Start test run: ${data.remainingEpochs} - $testGameNumber")
        val startPlayer = if (testGameNumber % 2 == 0) Player.Cross else Player.Circle
        loop(testGameNumber - 1, doGame(startPlayer, data))
      } else {
        data
      }
    }

    def doGame(startPlayer: Player, data: TestGameData): TestGameData = {
      val gameController = GameControllerImpl(strategyBuilder, startPlayer)

      val aiPlayer = aiTrainer.getNewAiPlayer(gameController, training = false)
      val logicPlayer = new LogicPlayer(Player.Circle, testGameRandom, possibleWinActions)

      gameController.startGame(aiPlayer, logicPlayer) match {
        case Some(Player.Cross) => data.copy(wonGames = data.wonGames + 1)
        case Some(Player.Circle) => data.copy(lostGames = data.lostGames + 1)
        case None => if (startPlayer == Player.Cross) {
          data.copy(drawGamesOffense = data.drawGamesOffense + 1)
        } else {
          data.copy(drawGamesDefense = data.drawGamesDefense + 1)
        }
      }
    }

    val data = loop(noOfTestGames, TestGameData(remainingEpochs, 0, 0, 0, 0))

    val epochs = data.remainingEpochs
    val wonGames = data.wonGames
    val lostGames = data.lostGames
    val drawGamesOff = data.drawGamesOffense
    val drawGamesDef = data.drawGamesDefense
    val totalGames = wonGames + lostGames + drawGamesDef + drawGamesOff
    info(f"$epochs: + $wonGames  - $lostGames  / $drawGamesDef  o $drawGamesOff => ${(totalGames - lostGames).toFloat * 100 / totalGames}%.2f%%")
    watcher.addEpochResult(Watcher.EpochResult(epochs, wonGames, lostGames, data.drawGamesOffense, data.drawGamesDefense))
  }

  def runUiGame(): Unit = {
    def gameLoop(testGameNumber: Int): Unit = {
      val gameName = s"testGame-$testGameNumber"
      clientMain.getNewStage(gameName).foreach { gameUi: GameUiStage =>
        doGame(gameName, gameUi)
        gameLoop(testGameNumber + 1)
      }(platform.executionContext)
    }

    def doGame(gameName: String, gameUi: GameUiStage): Unit = {
      info(s"run testGame: $gameName")
      val startPlayer = if (random.nextBoolean()) Player.Cross else Player.Circle
      val gameController = GameControllerImpl(strategyBuilder, startPlayer)

      val uiView = new UiView(gameUi, gameController.getGrid())
      val uiPlayer = new UiPlayerController(gameUi, Player.Circle)
      gameController.subscribe(uiView)
      val aiPlayer = aiTrainer.getNewAiPlayer(gameController, training = false)
      val result = gameController.startGame(aiPlayer, uiPlayer)
      info {
        result match {
          case Some(Player.Circle) => s"Human-Player wins"
          case Some(Player.Cross) => s"AI-Player wins"
          case None => "No winner in this game"
        }
      }
    }
    gameLoop(0)
  }
}
