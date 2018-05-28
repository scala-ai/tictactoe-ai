package de.ai.htwg.tictactoe

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy4xBuilder

object BruteForcePossibilities extends App {
  val start = System.currentTimeMillis()

  val allStrats = TTTWinStrategy4xBuilder.listAllWinStrategies
  val dim = 4
  val dimSq = 16


  val allGridPos = Array.ofDim[GridPosition](dimSq)

  for {
    x <- 0 until dim
    y <- 0 until dim
  } yield {
    allGridPos(x * dim + y) = GridPosition(x, y)
  }


  def factorial(number: Int): Long = {
    var result = 1L
    var factor = 2L
    while (factor <= number) {
      result *= factor
      factor += 1
    }
    result

  }

  // 1 up to 9
  // 0 = 1 => 8 = 9
  val allFactrial = Array.ofDim[Long](10)
  allFactrial(0) = 0
  for {
    f <- 1 to 9
  } {
    allFactrial(f) = factorial(f)
  }

  println("start")
  // Cross is starting

  var noCross = 0
  var noCircle = 0

  val map = mutable.Map[GridPosition, Player]()

  def rek(idx: Int): Unit = {
    for {
      tpe <- 0 until 3
    } {
      val pos = allGridPos(idx)
      tpe match {
        case 0 =>
        // nothing
        case 1 =>
          noCross += 1
          map.put(pos, Player.Cross)
        case 2 =>
          noCircle += 1
          map.put(pos, Player.Circle)
      }
      if (idx > 0) {
        rek(idx - 1)
      } else {
        collectData()
      }

      tpe match {
        case 0 =>
        // nothing
        case 1 =>
          noCross -= 1
          map.remove(pos)
        case 2 =>
          noCircle -= 1
          map.remove(pos)
      }

    }
  }

  var noTotal = 0
  var noWins = 0
  var noLoss = 0
  var noDraw = 0
  var noNotFinished = 0
  var illegalEndStates = 0

  var IllegalStates = 0L
  var movesToReachEnd = 0L

  val noWinsInStep = Array.ofDim[Int](dimSq)
  val noLossInStep = Array.ofDim[Int](dimSq)
  val noDrawInStep = Array.ofDim[Int](dimSq)
  val noRunningInStep = Array.ofDim[Int](dimSq)

  def collectData(): Unit = {
    val step = (noCircle + noCross) - 1
    noTotal += 1
    if (noTotal % 100000 == 0) println("#total: " + noTotal)
    checkBoardState() match {
      case 1 =>
        noWinsInStep(step) = noWinsInStep(step) + 1
        noWins += 1
        calcIllegalStates()
      case 2 =>
        noLossInStep(step) = noLossInStep(step) + 1
        noLoss += 1
        calcIllegalStates()
      case 3 =>
        noDrawInStep(step) = noDrawInStep(step) + 1
        noDraw += 1
        calcIllegalStates()
      case 4 =>
        noRunningInStep(step) = noRunningInStep(step) + 1
        noNotFinished += 1
      case 5 =>
        illegalEndStates += 1
    }
  }

  def calcIllegalStates(): Unit = {
    val remaining = dimSq - noCircle - noCross

    val posToReachState = allFactrial(noCircle) * allFactrial(noCross)

    movesToReachEnd += posToReachState
    IllegalStates += allFactrial(remaining) // * posToReachState
  }

  // 1 cross win
  // 2 circle win
  // 3 draw
  // 4 no end
  // 5 illegal
  def checkBoardState(): Int = {
    // cross fängt an, darf also 1 mehr sein
    // es müssen mindestens 4 gesetzt sein um zu gewinnen
    if (noCross < dim) return 5

    if (noCross == noCircle) {
      // circle kann gewonnen haben, wenn gesamt 16 kann auch untendschieden sein
      checkBoardWon(Player.Circle) match {
        case 0 => /* legal noWin */
          if (noCross + noCircle == dimSq) {
            // untendschieden
            return 3
          } else {
            return 4
            // no end state
          }

        case 1 => /* legal win */ return 2
        case 2 => /* illegal */ return 5
      }


    } else if (noCross == noCircle + 1) {
      // cross kann gewonnen haben. kein unentschieden
      checkBoardWon(Player.Cross) match {
        case 1 => /* legal win */
          return 1
        case 0 => /* legal noWin */
          return 4
        // no end state
        case 2 => /* illegal */
          return 5
      }
    } else {
      return 5
    }

  }


  // case 0 => /* legal noWin */
  // case 1 => /* legal win */
  // case 2 => /* illegal */
  def checkBoardWon(player: Player): Int = {
    val opponent = Player.other(player)
    if (allStrats.exists(_.check(pos => map.get(pos).contains(opponent)))) return 2

    val builder = ListBuffer[List[GridPosition]]()
    allStrats.foreach { winStrat =>
      if (winStrat.check(pos => map.get(pos).contains(player))) {
        builder += winStrat.list
      }
    }
    val result = builder.result()
    if (result.isEmpty) return 0
    if (result.size == 1) return 1

    // if more than one winning result => there must be at least 1 pos that is set in all of them.
    var list = result.head
    result.tail.foreach { next =>
      list = list.intersect(next)
    }

    if (list.isEmpty) 2 else 1
  }

  rek(dimSq - 1)


  val time = System.currentTimeMillis() - start
  println(s"finished in $time ms")


  println(" noTotal = " + noTotal)
  println(" noWins = " + noWins)
  println(" noLoss = " + noLoss)
  println(" noDraw = " + noDraw)
  println(" illegalEndStates = " + illegalEndStates)

  println(" movesToReachEnd = " + movesToReachEnd)

  println(" noWinsInStep = " + noWinsInStep.mkString(", "))
  println(" noLossInStep = " + noLossInStep.mkString(", "))
  println(" noDrawInStep = " + noDrawInStep.mkString(", "))
  println(" noRunningInStep = " + noRunningInStep.mkString(", "))


  println(" IllegalStates = " + IllegalStates)

}
