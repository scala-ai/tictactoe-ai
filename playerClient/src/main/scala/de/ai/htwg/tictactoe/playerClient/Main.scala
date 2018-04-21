package de.ai.htwg.tictactoe.playerClient

import scalafx.application.JFXApp
import scalafx.application.Platform
import scalafx.application.JFXApp.PrimaryStage

object Main extends App {

  private val app = new JFXApp {

    Platform.implicitExit = false

    stage = new PrimaryStage {
      title = ""
      width = 200
      height = 20
    }

    Platform.runLater {
      stage.hide()
    }
  }


  app.main(Array())

}