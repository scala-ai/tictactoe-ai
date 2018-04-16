package de.ai.scala.aiClient

import de.ai.scala.aiClient.learning.action.Action
import de.ai.scala.aiClient.learning.state.State

trait PlayerController[S >: State, A >: Action] {
  def getAction(state: S): A
}
