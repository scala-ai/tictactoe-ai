package de.ai.htwg.tictactoe

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.strategy.TTTWinStrategy4xBuilder

object BruteForcePossibilities extends App {
  val start = System.currentTimeMillis()

  val allStrats = TTTWinStrategy4xBuilder.listAllWinStrategies

  val allGridPos = Array.ofDim[GridPosition](16)

  for {
    x <- 0 until 4
    y <- 0 until 4
  } yield {
    allGridPos(x * 4 + y) = GridPosition(x, y)
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
  var illegalEndStates = 0

  var IllegalStates = 0L

  def collectData(): Unit = {
    noTotal += 1
    if (noTotal % 100000 == 0) println("#total: " + noTotal)
    checkBoardState() match {
      case 1 =>
        noWins += 1
        calcIllegalStates()
      case 2 =>
        noLoss += 1
        calcIllegalStates()
      case 3 =>
        noDraw += 1
      case 4 =>
        illegalEndStates += 1
    }
  }

  def calcIllegalStates(): Unit = {
    val remaining = 16 - noCircle - noCross
    IllegalStates += allFactrial(remaining)
  }

  // 1 cross win
  // 2 circle win
  // 3 draw
  // 4 no end or illegal
  def checkBoardState(): Int = {
    // cross fängt an, darf also 1 mehr sein
    // es müssen mindestens 4 gesetzt sein um zu gewinnen
    if (noCross < 4) return 4

    if (noCross == noCircle) {
      // circle kann gewonnen haben, wenn gesamt 16 kann auch untendschieden sein
      checkBoardWon(Player.Circle) match {
        case 0 => /* legal noWin */
          if (noCross + noCircle == 16) {
            // untendschieden
            return 3
          } else {
            return 4
            // no end state
          }

        case 1 => /* legal win */ return 2
        case 2 => /* illegal */ return 4
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
          return 4
      }
    } else {
      return 4
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

  rek(15)


  val time = System.currentTimeMillis() - start
  println(s"finished in $time ms")


  println(" noTotal = " + noTotal)
  println(" noWins = " + noWins)
  println(" noLoss = " + noLoss)
  println(" noDraw = " + noDraw)
  println(" illegalEndStates = " + illegalEndStates)

  println(" IllegalStates = " + IllegalStates)

}
