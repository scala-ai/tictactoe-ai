package de.ai.htwg.tictactoe.aiClient.learning.core

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.core.state.EpochResult
import de.ai.htwg.tictactoe.aiClient.learning.core.state.State

trait Learning[S <: State, A <: Action] {

  /**
   * This is a decision which is called while training is running. The decided
   * result depends on a policy of the implemented learning and is not always
   * the calculated best action in the given state.
   *
   * Updates the learning object and returns a new updated one. The returned
   * action could be trained later.
   *
   * @param state current model state
   * @return an updated learning object and the action to do
   */
  def getTrainingDecision(state: S): (Learning[S, A], A)

  /**
   * This is a decision which is called while a productive or test run. The
   * decided is always the calculated best action in the given state.
   *
   * Updates the learning object and returns a new updated one. The returned
   * action could be trained later.
   *
   * @param state current model state
   * @return an updated learning object and the action to do
   */
  def getBestDecision(state: S): (Learning[S, A], A)

  def getMinimaxDecision(state: S, nextState: (S, A) => S): (Learning[S, A], A)

  /**
   * This should be called after end of a training epoch. The learning object
   * trains internal all transitions which are processed in this epoch and
   * returns a new trained and clean learning object for the next epoch.
   * The given reward is trained for all done transitions until the last
   * training.
   *
   * @param reward epoch reward
   * @return updated and trained learning object
   */
  def trainHistory(reward: EpochResult): Learning[S, A]
}
