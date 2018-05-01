package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.core.state.EpochResult

case class TTTEpochResult private(
    win: Boolean,
    undecided: Boolean
) extends EpochResult

object TTTEpochResult {
  def won: TTTEpochResult = TTTEpochResult(win = true, undecided = false)

  def lost: TTTEpochResult = TTTEpochResult(win = false, undecided = false)

  def undecided: TTTEpochResult = TTTEpochResult(win = false, undecided = true)
}
