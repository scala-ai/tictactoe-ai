lazy val logicClient = TicTacToeBuild.defaultProject(project).settings(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.5.8",
  )
).dependsOn(
  TicTacToeBuild.clientConnection,
  TicTacToeBuild.gameLogic,
)