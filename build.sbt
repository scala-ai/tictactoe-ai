name := "tictactoe-ai"

version := "0.1"

scalaVersion := "2.12.5"

// CORE DL4J functionality
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-core" % "0.9.1"
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-nlp" % "0.9.1"
libraryDependencies += "org.deeplearning4j" % "rl4j-core" % "0.9.1"

// ND4J backend. You need one in every DL4J project. Normally define artifactId as
// either "nd4j-native-platform" or "nd4j-cuda-7.5-platform"
libraryDependencies += "org.nd4j" % "nd4j-native-platform" % "0.9.1"

// json formatting
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.8"

// testing
libraryDependencies += "org.scalamock" %% "scalamock" % "4.1.0" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test

lazy val administration = TicTacToeBuild.administration
lazy val aiClient = TicTacToeBuild.aiClient
lazy val clientConnection = TicTacToeBuild.clientConnection
lazy val gameLogic = TicTacToeBuild.gameLogic
lazy val playerClient = TicTacToeBuild.playerClient

