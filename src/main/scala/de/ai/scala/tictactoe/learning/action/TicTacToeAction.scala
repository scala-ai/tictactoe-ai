package de.ai.scala.tictactoe.learning.action

case class TicTacToeAction(
    coordinate: (Int, Int),
    player: Int
) extends Action