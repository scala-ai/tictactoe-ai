import sbt.Keys.libraryDependencies

lazy val aiClient = TicTacToeBuild.defaultProject(project).settings(
  libraryDependencies ++= Seq(
    // CORE DL4J functionality
    "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-alpha",
    "org.deeplearning4j" % "deeplearning4j-nlp" % "1.0.0-alpha",
    "org.deeplearning4j" % "rl4j-core" % "1.0.0-alpha",

    // ND4J backend. You need one in every DL4J project. Normally define artifactId as
    // either "nd4j-native-platform" or "nd4j-cuda-7.5-platform"
    "org.nd4j" % "nd4j-native-platform" % "1.0.0-alpha",

    // testing
    "org.scalamock" %% "scalamock" % "4.1.0" % Test,
  )
).dependsOn(
  TicTacToeBuild.clientConnection,
  TicTacToeBuild.gameLogic,
)