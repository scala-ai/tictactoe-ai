package de.ai.htwg.tictactoe.clientConnection.fxUI

import java.util.concurrent.CountDownLatch

import akka.actor.Actor
import akka.actor.Props
import de.ai.htwg.tictactoe.clientConnection.fxUI.UiMainActor.ReturnGameUI
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.Platform

object UiMainActor {

  @volatile
  private[UiMainActor] var hasStarted = false

  def props(dimensions: Int): Props = Props(new UiMainActor(dimensions))

  case class CreateGameUI(name: String)

  case class ReturnGameUI(gameUi: GameUiStage)
}

class UiMainActor private(dimensions: Int) extends Actor {
  // just to make sure there is only ever one one ui actor.
  UiMainActor.synchronized {
    if (UiMainActor.hasStarted) throw new IllegalStateException("ClientMainActor already started")
    UiMainActor.hasStarted = true
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


  private def handleAddStage(name: String): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val futureStage = GameUiStage(name, dimensions)
    val receiver = sender()
    futureStage.foreach(stage => receiver.tell(ReturnGameUI(stage), self))
  }


  override def postStop(): Unit = {
    Platform.exit()
    appThread.join()
    UiMainActor.synchronized {
      UiMainActor.hasStarted = false
    }
  }

  override def receive: Receive = {
    case UiMainActor.CreateGameUI(name: String) => handleAddStage(name)
  }
}


