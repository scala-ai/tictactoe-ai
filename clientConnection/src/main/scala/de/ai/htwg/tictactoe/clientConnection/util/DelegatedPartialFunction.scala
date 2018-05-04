package de.ai.htwg.tictactoe.clientConnection.util

trait DelegatedPartialFunction[A, B] extends PartialFunction[A, B] {
  def pf: PartialFunction[A, B]

  private val p: PartialFunction[A, B] = pf
  override def isDefinedAt(x: A): Boolean = p.isDefinedAt(x)
  override def apply(v1: A): B = p.apply(v1)
}
