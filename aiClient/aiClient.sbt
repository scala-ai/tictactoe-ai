import sbt.Keys.libraryDependencies

lazy val aiClient = TicTacToeBuild.defaultProject(project).settings(
  // CORE DL4J functionality
  libraryDependencies ++= Seq(
    "org.deeplearning4j" % "deeplearning4j-core" % "0.9.1",
    "org.deeplearning4j" % "deeplearning4j-nlp" % "0.9.1",
    "org.deeplearning4j" % "rl4j-core" % "0.9.1",

    // ND4J backend. You need one in every DL4J project. Normally define artifactId as
    // either "nd4j-native-platform" or "nd4j-cuda-7.5-platform"
    "org.nd4j" % "nd4j-native-platform" % "0.9.1",

    // json formatting
    "com.typesafe.play" %% "play-json" % "2.6.8",
  )
)