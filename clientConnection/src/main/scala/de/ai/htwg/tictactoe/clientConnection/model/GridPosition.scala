package de.ai.htwg.tictactoe.clientConnection.model

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

case class GridPosition(x: Int, y: Int) {
  // Fixme
  @inline private def inRange = GridPosition.inRange _
  def error(cord: Char, i: Int) = s"$cord coordinate must be between 0 and 3 (inclusive) but is: $i"
  require(inRange(x), error('X', x))
  require(inRange(y), error('Y', y))

  def incrementX: Option[GridPosition] = Some(x + 1).filter(inRange).map(x => copy(x = x))
  def decrementX: Option[GridPosition] = Some(x - 1).filter(inRange).map(x => copy(x = x))
  def incrementY: Option[GridPosition] = Some(y + 1).filter(inRange).map(y => copy(y = y))
  def decrementY: Option[GridPosition] = Some(y - 1).filter(inRange).map(y => copy(y = y))

  def buildCombinationsOf4: List[List[GridPosition]] = GridPosition.buildCombinationsOf4(this)
}

object GridPosition {
  def inRange(i: Int): Boolean = i >= 0 && i <= 3

  def buildCombinationsOf4(pos: GridPosition): List[List[GridPosition]] = {
    List(
      horizontalLine(pos),
      verticalLine(pos),
      risingLine(pos),
      fallingLine(pos),
      topLeftBlock(pos),
      topRightBlock(pos),
      bottomLeftBlock(pos),
      bottomRightBlock(pos),
    )
  }

  def horizontalLine(pos: GridPosition): List[GridPosition] = buildLine(getLast(pos, _.decrementX), _.incrementX)
  def verticalLine(pos: GridPosition): List[GridPosition] = buildLine(getLast(pos, _.decrementY), _.incrementY)
  def risingLine(pos: GridPosition): List[GridPosition] = buildLine(getLast(pos, _.decrementX.flatMap(_.decrementY)), _.incrementX.flatMap(_.incrementY))
  def fallingLine(pos: GridPosition): List[GridPosition] = buildLine(getLast(pos, _.decrementX.flatMap(_.incrementY)), _.incrementX.flatMap(_.decrementY))

  def topLeftBlock(pos: GridPosition): List[GridPosition] = buildBlock(pos, _.incrementY, _.decrementX)
  def topRightBlock(pos: GridPosition): List[GridPosition] = buildBlock(pos, _.incrementY, _.incrementX)
  def bottomLeftBlock(pos: GridPosition): List[GridPosition] = buildBlock(pos, _.decrementY, _.decrementX)
  def bottomRightBlock(pos: GridPosition): List[GridPosition] = buildBlock(pos, _.decrementY, _.incrementX)

  private def buildLine(start: GridPosition, step: GridPosition => Option[GridPosition]): List[GridPosition] = {
    val lineBuff = ListBuffer[GridPosition]()
    lineBuff += start

    @tailrec
    def rec(current: GridPosition): List[GridPosition] = step(current) match {
      case None => lineBuff.result()
      case Some(next) =>
        lineBuff += next
        rec(next)
    }
    rec(start)
  }

  private def getLast(start: GridPosition, step: GridPosition => Option[GridPosition]): GridPosition = {
    @tailrec
    def rec(current: GridPosition): GridPosition = step(current) match {
      case None => current
      case Some(next) => rec(next)
    }
    rec(start)
  }

  private def buildBlock(pos: GridPosition, first: GridPosition => Option[GridPosition], second: GridPosition => Option[GridPosition]): List[GridPosition] = {
    val optList = for {
      f <- first(pos)
      s <- second(pos)
      fs <- second(f)
    } yield {
      List(pos, f, s, fs)
    }
    optList.toList.flatten
  }

}
