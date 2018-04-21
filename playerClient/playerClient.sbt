lazy val playerClient = TicTacToeBuild.defaultProject(project).settings(
  libraryDependencies ++= Seq(
    "org.scalafx" %% "scalafx" % "8.0.144-R12",
    "com.typesafe.akka" %% "akka-actor" % "2.5.8",
  )
).dependsOn(
  TicTacToeBuild.clientConnection,
)