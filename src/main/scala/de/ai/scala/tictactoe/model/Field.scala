package de.ai.scala.tictactoe.model

sealed trait Field {
  def asDoubleVal(): Double
}

object Field {
  case class Circle() extends Field {
    override def asDoubleVal(): Double = -1.0
  }

  case class Cross() extends Field {
    override def asDoubleVal(): Double = 1.0
  }

  case class Empty() extends Field {
    override def asDoubleVal(): Double = 0.0
  }
}
