import sbt.project
import sbt.Keys
import sbt.Def
import sbt.file
import sbt.Project
import sbt.stringToOrganization
import sbt.Keys.scalacOptions


object TicTacToeBuild {

  lazy val administration = project
  lazy val aiClient = project
  lazy val clientConnection = project
  lazy val gameLogic = project
  lazy val playerClient = project

  val scalaVersionNumber = "2.12.5"
  val scalaVersion = Keys.scalaVersion := scalaVersionNumber

  val scalaOptions = scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    // "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    // FIXME This is a confirmed Scala bug in 2.12 Xlint will produce Position.point on NoPosition Error
    // "-Xlint", // Enable recommended additional warnings.
    "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
    "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
    "-Ywarn-numeric-widen" // Warn when numerics are widened.
  )


  val standardLibraries = Keys.libraryDependencies ++= Seq(
    // injection
    "net.codingwell" %% "scala-guice" % "4.1.1",
    // test
    "org.scalatest" %% "scalatest" % "3.0.4" % "test",
    // logging
    "org.clapper" %% "grizzled-slf4j" % "1.3.2"
  )
  val defaultSettings: Seq[Def.SettingsDefinition] = Seq(
    scalaOptions,
    scalaVersion,
    standardLibraries
  )


  /**
   * change project to current file
   */
  def inCurrent(project: Project): Project = project in file(".")

  /**
   * change project to current file and add defaultSettings
   */
  def defaultProject(project: Project): Project = inCurrent(project).settings(defaultSettings: _*)

}
