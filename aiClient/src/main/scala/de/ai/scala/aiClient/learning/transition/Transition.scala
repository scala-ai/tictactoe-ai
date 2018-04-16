package de.ai.scala.aiClient.learning.transition

import de.ai.scala.aiClient.learning.action.Action
import de.ai.scala.aiClient.learning.state.State

trait Transition[A <: Action, S <: State] {
  val observation: S
  val action: A
  val reward: Double
}
