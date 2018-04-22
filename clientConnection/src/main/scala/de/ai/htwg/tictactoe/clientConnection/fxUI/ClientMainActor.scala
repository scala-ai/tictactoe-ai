package de.ai.htwg.tictactoe.clientConnection.fxUI

import java.util.concurrent.CountDownLatch

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import de.ai.htwg.tictactoe.clientConnection.fxUI.ClientMainActor.ReturnGameUI
import scalafx.application.JFXApp
import scalafx.application.Platform
import scalafx.application.JFXApp.PrimaryStage

object ClientMainActor {

  @volatile
  private[ClientMainActor] var hasStarted = false

  def props(dimensions: Int): Props = Props(new ClientMainActor(dimensions))

  case class CreateGameUI(name: String)
  case class ReturnGameUI(gameUiActor: ActorRef)
}

class ClientMainActor private(dimensions: Int) extends Actor {
  // just to make sure there is only ever one one ui actor.
  ClientMainActor.synchronized {
    if (ClientMainActor.hasStarted) throw new IllegalStateException("ClientMainActor already started")
    ClientMainActor.hasStarted = true
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
    val gUI = context.actorOf(GameUiActor.props(name, dimensions), name)
    sender() ! ReturnGameUI(gUI)
  }


  override def postStop(): Unit = {
    Platform.exit()
    appThread.join()
    ClientMainActor.synchronized {
      ClientMainActor.hasStarted = false
    }
  }
  override def receive: Receive = {
    case ClientMainActor.CreateGameUI(name: String) => handleAddStage(name)
  }
}


