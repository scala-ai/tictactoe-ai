package de.ai.htwg.tictactoe.clientConnection.fxUI

import java.util.concurrent.CountDownLatch

import scala.concurrent.Future

import javax.inject.Singleton
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.Platform

object UiMain {

  @volatile
  private[UiMain] var hasStarted = false

  def apply(dimensions: Int): UiMain = new UiMain(dimensions)

}

@Singleton
class UiMain private(dimensions: Int) {
  // just to make sure there is only ever one one ui actor.
  UiMain.synchronized {
    if (UiMain.hasStarted) throw new IllegalStateException("ClientMainActor already started")
    UiMain.hasStarted = true
  }

  private val appStartedLatch = new CountDownLatch(1)
  private val app = new JFXApp {

    Platform.implicitExit = false

    stage = new PrimaryStage {
      title = ""
      width = 200
      height = 20
    }

    Platform.runLater {
      stage.hide()
      appStartedLatch.countDown()
    }
  }
  private val appThread = new Thread(() => {
    app.main(Array())
  })
  appThread.start()
  // the overhead for handling this completely async is a lot more than simply sleeping once.
  appStartedLatch.await()


  def getNewStage(name: String): Future[GameUiStage] = {
    GameUiStage(name, dimensions)
  }


  def stop(): Unit = {
    Platform.exit()
    appThread.join()
    UiMain.synchronized {
      UiMain.hasStarted = false
    }
  }

}


