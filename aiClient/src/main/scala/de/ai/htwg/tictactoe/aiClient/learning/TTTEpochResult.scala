package de.ai.htwg.tictactoe.aiClient.learning

import de.ai.htwg.tictactoe.aiClient.learning.core.state.EpochResult

case class TTTEpochResult(
    win: Boolean
) extends EpochResult
