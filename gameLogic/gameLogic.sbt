lazy val gameLogic = TicTacToeBuild.defaultProject(project).settings(
  libraryDependencies ++= Seq(
    // actor
    "com.typesafe.akka" %% "akka-actor" % "2.5.8",
    // json
    "com.typesafe.play" %% "play-json" % "2.6.8",
    //akka test
    "com.typesafe.akka" %% "akka-testkit" % "2.5.8" % Test,
  )
)