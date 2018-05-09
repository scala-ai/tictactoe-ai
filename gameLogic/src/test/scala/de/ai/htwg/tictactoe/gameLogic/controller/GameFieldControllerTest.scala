package de.ai.htwg.tictactoe.gameLogic.controller

import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GameField
import org.scalatest.FreeSpec
import org.scalatest.Matchers
import org.scalatest.Inside

class GameFieldControllerTest extends FreeSpec with Matchers with Inside {

  private def fixture(): GameFieldController = {
    val cont = new GameFieldController(TTTWinStrategy4xBuilder)
    cont.setPos(0, 0, Player.Cross)
    cont.setPos(1, 0, Player.Circle)
    cont.setPos(0, 1, Player.Cross)
    cont.setPos(1, 1, Player.Circle)
    cont.setPos(0, 2, Player.Cross)
    cont.setPos(1, 2, Player.Circle)
    cont
  }

  "A game where PLayer ONE starts will finish with" - {

    "Player ONE winning" in {
      val controller = fixture()
      inside(controller.setPos(0, 3, Player.Cross)) { case GameFieldController.Result.GameFinished(f, winner @ Some(Player.Cross)) =>
        f.isCurrentPlayer(Player.Cross) shouldBe false
        f.isCurrentPlayer(Player.Circle) shouldBe false
        f.gameState should matchPattern { case GameField.Finished(`winner`) => }
      }
    }


    "Player TWO winning" in {
      val controller = fixture()
      inside(controller.setPos(2, 0, Player.Cross)) { case GameFieldController.Result.GameUpdated(f) =>
        f.isCurrentPlayer(Player.Cross) shouldBe false
        f.isCurrentPlayer(Player.Circle) shouldBe true
        f.gameState should matchPattern { case GameField.Running(Player.Circle) => }
      }

      inside(controller.setPos(1, 3, Player.Circle)) { case GameFieldController.Result.GameFinished(f, winner @ Some(Player.Circle)) =>
        f.isCurrentPlayer(Player.Cross) shouldBe false
        f.isCurrentPlayer(Player.Circle) shouldBe false
        f.gameState should matchPattern { case GameField.Finished(`winner`) => }
      }
    }
  }

}
