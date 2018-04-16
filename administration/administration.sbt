lazy val administration = TicTacToeBuild.defaultProject(project).dependsOn(
  TicTacToeBuild.gameLogic,
  TicTacToeBuild.clientConnection,
)