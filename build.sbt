name := "tictactoe-ai"

version := "0.1"

scalaVersion := "2.12.5"

lazy val administration = TicTacToeBuild.administration
lazy val aiClient = TicTacToeBuild.aiClient
lazy val clientConnection = TicTacToeBuild.clientConnection
lazy val gameLogic = TicTacToeBuild.gameLogic
lazy val playerClient = TicTacToeBuild.playerClient
