package de.ai.htwg.tictactoe.aiClient

import de.ai.htwg.tictactoe.aiClient.learning.TTTAction
import de.ai.htwg.tictactoe.aiClient.learning.TTTLearningProcessor
import de.ai.htwg.tictactoe.aiClient.learning.TTTState
import de.ai.htwg.tictactoe.clientConnection.gameController.GameControllerMultiPlayer
import de.ai.htwg.tictactoe.clientConnection.gameController.RatedGameControllerPlayer
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import grizzled.slf4j.Logging


class AiPlayer(
    var learningUnit: TTTLearningProcessor,
    override val currentPlayer: Player,
    isStartingPlayer: Boolean,
    training: Boolean,
) extends GameControllerMultiPlayer with RatedGameControllerPlayer with Logging {
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

  override def getScores(field: GameField, moves: List[GridPosition]): List[RatedGameControllerPlayer.RatedMove] = {
    // when not current player => reverse result

    val (factor, starting) = field.gameState.asRunning match {
      case None => throw new IllegalStateException("cannot get scores for game that is already finished.")
      case Some(GameField.Running(`currentPlayer`)) => 1.0 -> isStartingPlayer
      case _ /* opponent */ => -1.0 -> !isStartingPlayer
    }

    val state = TTTState(field, starting)
    val actions = moves.map(gp => TTTAction(gp, field.dimensions))

    learningUnit.getRatedDecisions(state, actions).map {
      case (action, rating) => RatedGameControllerPlayer.RatedMove(action.coordinate, rating * factor)
    }
  }

}
