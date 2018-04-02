package de.ai.scala.tictactoe.model

case class CircleField() extends Field {
  override def asDoubleVal(): Double = -1.0
}

object CircleField {
  def apply(): CircleField = new CircleField()
}
