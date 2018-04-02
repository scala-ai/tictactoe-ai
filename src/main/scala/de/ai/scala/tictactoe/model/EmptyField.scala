package de.ai.scala.tictactoe.model

case class EmptyField() extends Field {
  override def asDoubleVal(): Double = 0.0
}

object EmptyField {
  def apply(): EmptyField = new EmptyField()
}
