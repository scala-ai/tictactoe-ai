package de.ai.htwg.tictactoe.clientConnection.util

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

class SingleThreadPlatform private(private val thread: Thread, val executionContext: ExecutionContext) {
  def isNotCurrentThread: Boolean = Thread.currentThread() != thread

  def isCurrentThread: Boolean = Thread.currentThread() == thread

  def checkCurrentThread(): Unit = {
    if (isNotCurrentThread) throw new IllegalStateException("Illegal Thread")
  }

  def execute(func: => Unit): Unit = executionContext.execute(() => func)
}

object SingleThreadPlatform {
  def apply(): SingleThreadPlatform = {
    val promise = concurrent.Promise[Runnable]()
    val thread = new Thread(() => {
      Await.result(promise.future, Duration(100, "ms")).run()
    })

    val f: ThreadFactory = r => {
      promise.success(r)
      thread
    }

    val executionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor(f))
    new SingleThreadPlatform(thread, executionContext)
  }
}
