lazy val clientConnection = TicTacToeBuild.defaultProject(project).settings(
  libraryDependencies ++= Seq(
    // json
    "com.typesafe.play" %% "play-json" % "2.6.8",
    "com.typesafe.akka" %% "akka-actor" % "2.5.8",
    "org.scalafx" %% "scalafx" % "8.0.144-R12",
  )
)