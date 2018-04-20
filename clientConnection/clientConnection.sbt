lazy val clientConnection = TicTacToeBuild.defaultProject(project).settings(
  libraryDependencies ++= Seq(
    // json
    "com.typesafe.play" %% "play-json" % "2.6.8",
  )
)