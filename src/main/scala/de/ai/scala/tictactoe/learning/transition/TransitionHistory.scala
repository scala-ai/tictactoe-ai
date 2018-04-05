package de.ai.scala.tictactoe.learning.transition

import de.ai.scala.tictactoe.learning.action.Action
import de.ai.scala.tictactoe.learning.state.State

trait TransitionHistory[A <: Action, S <: State] {
  def addTransition(transition: Transition[A])

  def next(): Transition[A]

  def hasNext: Boolean
}
