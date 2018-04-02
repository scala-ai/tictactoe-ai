package de.ai.scala.tictactoe.model

case class CrossField() extends Field {
  override def asDoubleVal(): Double = 1.0
}

object CrossField {
  def apply(): CrossField = new CrossField()
}
