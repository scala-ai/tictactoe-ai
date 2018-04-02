package de.ai.scala.tictactoe.learning.transition

import de.ai.scala.tictactoe.learning.action.Action

trait TransitionHistory[A >: Action] {
  def addTransition(transition: Transition[A])

  def next(): Transition[A]

  def hasNext: Boolean
}
