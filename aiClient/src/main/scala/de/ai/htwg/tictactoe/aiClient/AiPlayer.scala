package de.ai.htwg.tictactoe.aiClient

import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.TTTState
import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerMultiPlayer
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import grizzled.slf4j.Logging


class AiPlayer(
    var learningUnit: TTTLearningProcessor,
    override val currentPlayer: Player,
    isStartingPlayer: Boolean,
    training: Boolean,
) extends GameControllerMultiPlayer with Logging {
  trace(s"AiPlayer starts playing as $currentPlayer")

  override def getMove(grid: GameField): GridPosition = {
    trace("AiPlayer: current turn")
    // if aiPlayer is in training state calculate a training decision, else calculate best action
    val state = TTTState(grid, isStartingPlayer)
    val (action, newLearningUnit) = if (training) {
      learningUnit.getTrainingDecision(state)
    } else {
      learningUnit.getBestDecision(state)
    }
    learningUnit = newLearningUnit
    action.coordinate
  }

  override def getXMoves(x: Int, field: GameField): List[GridPosition] = {
    trace("AiPlayer: current turn")
    val state = TTTState(field, isStartingPlayer)
    val (actions, newLearningUnit) = learningUnit.getXBestDecisions(x, state)
    learningUnit = newLearningUnit
    actions.map(_.coordinate)
  }
}
