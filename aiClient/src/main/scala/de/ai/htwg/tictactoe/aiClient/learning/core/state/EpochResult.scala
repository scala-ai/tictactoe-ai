package de.ai.htwg.tictactoe.aiClient.learning.core.state

sealed trait EpochResult {
}

object EpochResult {
  case object Won extends EpochResult
  case object Lost extends EpochResult
  case object Draw extends EpochResult
}
