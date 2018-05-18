package de.ai.htwg.tictactoe.clientConnection.util

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

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
      throw new IllegalStateException("PlatformThread closing")
    })

    @volatile var called = false
    val f: ThreadFactory = r => {
      if (called) {
        Thread.sleep(10000)
        throw new IllegalStateException("Factory called more than once.")
      }
      called = true
      promise.success(r)
      thread
    }

    val threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue[Runnable], f)
    println("threadpool allows corethread timeout = " + threadPoolExecutor.allowsCoreThreadTimeOut())
    val executionContext = ExecutionContext.fromExecutor(threadPoolExecutor)
    new SingleThreadPlatform(thread, executionContext)
  }
}
