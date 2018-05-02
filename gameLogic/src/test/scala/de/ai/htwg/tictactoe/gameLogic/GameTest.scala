package de.ai.htwg.tictactoe.gameLogic

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

import akka.actor.ActorRef
import akka.pattern.ask
import akka.pattern.gracefulStop
import akka.util.Timeout
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.SelectPosition
import de.ai.htwg.tictactoe.gameLogic.controller.GameFieldControllerActor.SelectPositionAck


class GameTest extends GameLogicTestSpec[ActorRef] {
  implicit val duration: FiniteDuration = Duration(2, TimeUnit.SECONDS)
  implicit val timeout: Timeout = Timeout(duration)
  implicit val context: ExecutionContextExecutor = system.dispatcher
  private val dimensions = 4
  private val posBuilder = GridPosition(dimensions)

  private val GFCA = GameFieldControllerActor

  override def setupFixture(): Future[ActorRef] = {
    val controller: ActorRef = system.actorOf(GameFieldControllerActor.props(Player.Cross, dimensions))
    for {
      _ <- controller ? GFCA.SelectPosition(Player.Cross, posBuilder(0, 0))
      _ <- controller ? GFCA.SelectPosition(Player.Circle, posBuilder(1, 0))
      _ <- controller ? GFCA.SelectPosition(Player.Cross, posBuilder(0, 1))
      _ <- controller ? GFCA.SelectPosition(Player.Circle, posBuilder(1, 1))
      _ <- controller ? GFCA.SelectPosition(Player.Cross, posBuilder(0, 2))
      _ <- controller ? GFCA.SelectPosition(Player.Circle, posBuilder(1, 2))
    } yield {
      controller
    }
  }


  override def cleanupFixture(controller: ActorRef): Future[Unit] = {
    gracefulStop(controller, duration).map(_ => ())
  }

  "A game where PLayer ONE starts will finish with" - {

    "Player ONE winning" in { controller =>
      val pos = posBuilder(0, 3)
      controller.tell(SelectPosition(Player.Cross, pos), testActor)

      expectMsgPF(duration) {
        case SelectPositionAck(field, GFCA.RetCode.GameWon) => field.isFinished shouldBe true
      }
    }


    "Player TWO winning" in { controller =>
      val pos1 = posBuilder(2, 0)
      val pos2 = posBuilder(1, 3)
      controller.ask(SelectPosition(Player.Cross, pos1)).onComplete { _ =>
        controller.tell(SelectPosition(Player.Circle, pos2), testActor)
      }

      expectMsgPF(duration) {
        case SelectPositionAck(field, GFCA.RetCode.GameWon) => field.isFinished shouldBe true
      }

    }
  }

}
