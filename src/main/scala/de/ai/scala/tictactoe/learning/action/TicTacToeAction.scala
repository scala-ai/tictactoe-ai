package de.ai.scala.tictactoe.learning.action

import de.ai.scala.tictactoe.model.Coordinate

case class TicTacToeAction(
    coordinate: Coordinate
) extends Action