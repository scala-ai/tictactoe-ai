package de.ai.scala.tictactoe.model

sealed trait Field {
  def asDoubleVal(): Double
}

case class CircleField() extends Field {
  override def asDoubleVal(): Double = -1.0
}

case class CrossField() extends Field {
  override def asDoubleVal(): Double = 1.0
}

case class EmptyField() extends Field {
  override def asDoubleVal(): Double = 0.0
}
