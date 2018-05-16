package de.ai.htwg.tictactoe.aiClient.learning.core.policy

import de.ai.htwg.tictactoe.aiClient.learning.core.action.Action
import de.ai.htwg.tictactoe.aiClient.learning.core.state.State

trait Policy[S <: State, A <: Action] {

  /**
   * Get next action by the implemented policy. Source is the current state
   * and this method returns the next action with the current reward (f.e. q value).
   *
   * @param state           current state
   * @param bestAction      supplier for the best action in this state
   * @param qValueSupplier  function to calculate a q value for a given state and action
   * @param possibleActions possible actions in the current state
   * @return the next action and the action's q value (if the returned state is NaN,
   *         a random action was selected and there is no q value available)
   */
  def nextAction(state: S, bestAction: () => (A, Double), qValueSupplier: (S, A) => Double, possibleActions: List[A]): (A, Double)

  def incrementEpoch(): Policy[S, A]

  def incrementStep(state: S): Policy[S, A]

  def resetSteps(): Policy[S, A]
}