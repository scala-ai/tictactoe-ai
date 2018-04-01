package de.ai.scala.tictactoe.transition

import de.ai.scala.tictactoe.action.Action

trait TransitionHistory[A >: Action] {
  def addTransition(transition: Transition[A])

  def next(): Transition[A]

  def hasNext: Boolean
}
