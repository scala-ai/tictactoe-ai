package de.ai.htwg.tictactoe.gameLogic

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.fixture
import org.scalatest.FutureOutcome
import org.scalatest.Outcome

abstract class GameLogicTestSpec[FP] extends TestKit(ActorSystem()) with fixture.AsyncFreeSpecLike with Matchers with BeforeAndAfterAll {

  override final protected type FixtureParam = FP

  def setupFixture(): Future[FP]

  def cleanupFixture(fp: FP): Future[Unit]

  private def toNoArgAsyncTest(test: OneArgAsyncTest)(execute: (FP => Future[Outcome]) => Future[Outcome]) = {
    new NoArgAsyncTest {
      val name = test.name
      val configMap = test.configMap
      // test(fp) can throw exception. For this reason it is packed into another future.
      def apply(): FutureOutcome = new FutureOutcome(execute { fp => Future(test(fp).toFuture).flatten })
      val scopes = test.scopes
      val text = test.text
      val tags = test.tags
      val pos = test.pos
    }
  }

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {

    val noArgTest = toNoArgAsyncTest(test) { execute =>
      setupFixture().flatMap { fp =>
        execute(fp).transformWith { resultTry =>
          cleanupFixture(fp).transform(_.flatMap(_ => resultTry))
        }
      }
    }
      withFixture(noArgTest)
  }
  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)
}

