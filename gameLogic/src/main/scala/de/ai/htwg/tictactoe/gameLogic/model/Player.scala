package de.ai.htwg.tictactoe.gameLogic.model

sealed trait Player

object Player {
  case object Cross extends Player
  case object Circle extends Player

  def other(p: Player): Player = p match {
    case Cross => Circle
    case Circle => Cross
  }
}
