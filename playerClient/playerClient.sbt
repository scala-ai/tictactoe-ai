lazy val playerClient = TicTacToeBuild.defaultProject(project).settings(
  libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.144-R12"
).dependsOn(
  TicTacToeBuild.clientConnection,
)