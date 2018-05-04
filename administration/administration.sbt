lazy val administration = TicTacToeBuild.defaultProject(project).dependsOn(
  TicTacToeBuild.gameLogic,
  TicTacToeBuild.clientConnection,
  TicTacToeBuild.aiClient,
  TicTacToeBuild.playerClient,
  TicTacToeBuild.logicClient,
)