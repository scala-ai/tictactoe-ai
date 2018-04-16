package de.ai.scala.aiClient.model

sealed trait Field {
  def asDoubleVal(): Double
}

object Field {
  case object Circle extends Field {
    override def asDoubleVal(): Double = -1.0
  }

  case object Cross extends Field {
    override def asDoubleVal(): Double = 1.0
  }

  case object Empty extends Field {
    override def asDoubleVal(): Double = 0.0
  }
}
