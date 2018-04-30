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
    "-Xlint:adapted-args", //               Warn if an argument list is modified to match the receiver.
    "-Xlint:nullary-unit", //               Warn when nullary methods return Unit.
    "-Xlint:inaccessible", //               Warn about inaccessible types in method signatures.
    "-Xlint:nullary-override", //           Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:infer-any", //                  Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", //       A string literal appears to be missing an interpolator id.
    "-Xlint:doc-detached", //               A Scaladoc comment appears to be detached from its element.
    "-Xlint:private-shadow", //             A private field (or class parameter) shadows a superclass field.
    "-Xlint:type-parameter-shadow", //      A local type parameter shadows a type already in scope.
    "-Xlint:poly-implicit-overload", //     Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:option-implicit", //            Option.apply used implicit view.
    "-Xlint:delayedinit-select", //         Selecting member of DelayedInit.
    "-Xlint:by-name-right-associative", //  By-name parameter of right associative operator.
    "-Xlint:package-object-classes", //     Class or object defined in package object.
    "-Xlint:unsound-match", //              Pattern match may not be typesafe.
    "-Xlint:stars-align", //                Pattern sequence wildcard must align with sequence component.
    "-Xlint:constant", //                   Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:unused", //                     Enable -Ywarn-unused:imports,privates,locals,implicits.
    "-deprecation", //                      Emit warning and location for usages of deprecated APIs.
    "-feature", //                          Emit warning and location for usages of features that should be imported explicitly.
    "-unchecked", //                        Enable additional warnings where generated code depends on assumptions.
    // "-Xfatal-warnings", //               Fail the compilation if there are any warnings.
    "-Ywarn-dead-code", //                  Warn when dead code is identified.
    "-Ywarn-numeric-widen" //               Warn when numerics are widened.
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
