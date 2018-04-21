package de.ai.htwg.tictactoe.playerClient

import java.util.concurrent.CountDownLatch

import scala.concurrent.ExecutionContext.Implicits.global

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import scalafx.application.JFXApp
import scalafx.application.Platform
import scalafx.application.JFXApp.PrimaryStage

object Main extends App {

  private val appStarted = new CountDownLatch(1)
  private val app = new JFXApp {

    Platform.implicitExit = false

    stage = new PrimaryStage {
      title = ""
      width = 200
      height = 20
    }

    Platform.runLater {
      stage.hide()
      appStarted.countDown()
    }
  }

  val thread = new Thread(() => {
    app.main(Array())
  })
  thread.start()

  appStarted.await()
  println("new Stage")


  val stage = GameUiStage("ttt", 4, set)

  def set(pos: GridPosition): Unit = {
    stage.foreach(_.cross(pos).set())
  }

  println("show")
  stage.foreach(_.show())

}