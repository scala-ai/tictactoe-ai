package de.ai.htwg.tictactoe.gameLogic.model

sealed trait Player

object Player {
  case object One extends Player
  case object Two extends Player

  def other(p: Player): Player = p match {
    case One => Two
    case Two => One
  }
}
