package de.ai.htwg.tictactoe.clientConnection.model.strategy

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition

case class TTTWinStrategy(list: List[GridPosition], requiredConnected: Int) {

  def check(checker: GridPosition => Boolean): Boolean = {

    def checkRequirement(list: List[GridPosition], connected: Int): Boolean = list match {
      case Nil => false
      case head :: tail =>
        val newConnected = if (checker(head)) connected + 1 else 0
        if (newConnected == requiredConnected) true else checkRequirement(tail, newConnected)
    }

    checkRequirement(list, 0)
  }
}
