package de.ai.htwg.tictactoe.clientConnection.model

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer


// abstract so the compiler does not generate apply and copy methods.
// sealed so it can only be instantiated in this file
// 2 param blocks as case class methods like equals ignore the 2nd block
sealed abstract case class GridPositionOLD private[GridPositionOLD](x: Int, y: Int)(private val builder: GridPositionOLD.Builder) extends {
  val dimensions: Int = builder.dimensions

  @inline private def inRange = builder.inRange _
  def incrementX: Option[GridPositionOLD] = Some(x + 1).filter(inRange).map(x => copy(x = x))
  def decrementX: Option[GridPositionOLD] = Some(x - 1).filter(inRange).map(x => copy(x = x))
  def incrementY: Option[GridPositionOLD] = Some(y + 1).filter(inRange).map(y => copy(y = y))
  def decrementY: Option[GridPositionOLD] = Some(y - 1).filter(inRange).map(y => copy(y = y))

  def buildConnectedCombinations: List[List[GridPositionOLD]] = GridPositionOLD.buildConnectedCombinations(this)

  private def copy(x: Int = this.x, y: Int = this.y) = new GridPositionOLD(x, y)(builder){}
}

object GridPositionOLD {

  class Builder private[GridPositionOLD](val dimensions: Int) {
    def apply(x: Int, y: Int): GridPositionOLD = {
      def error(cord: Char, i: Int) = s"$cord coordinate must be between 0 and 3 (inclusive) but is: $i"
      require(inRange(x), error('X', x))
      require(inRange(y), error('Y', y))

      new GridPositionOLD(x, y)(this) {}
    }

    def inRange(i: Int): Boolean = i >= 0 && i < dimensions
  }

  def apply(dimensions: Int) = new Builder(dimensions)

  def buildConnectedCombinations(pos: GridPositionOLD): List[List[GridPositionOLD]] = {
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

  def horizontalLine(pos: GridPositionOLD): List[GridPositionOLD] = buildLine(getLast(pos, _.decrementX), _.incrementX)
  def verticalLine(pos: GridPositionOLD): List[GridPositionOLD] = buildLine(getLast(pos, _.decrementY), _.incrementY)
  def risingLine(pos: GridPositionOLD): List[GridPositionOLD] = buildLine(getLast(pos, _.decrementX.flatMap(_.decrementY)), _.incrementX.flatMap(_.incrementY))
  def fallingLine(pos: GridPositionOLD): List[GridPositionOLD] = buildLine(getLast(pos, _.decrementX.flatMap(_.incrementY)), _.incrementX.flatMap(_.decrementY))

  def topLeftBlock(pos: GridPositionOLD): List[GridPositionOLD] = buildBlock(pos, _.incrementY, _.decrementX)
  def topRightBlock(pos: GridPositionOLD): List[GridPositionOLD] = buildBlock(pos, _.incrementY, _.incrementX)
  def bottomLeftBlock(pos: GridPositionOLD): List[GridPositionOLD] = buildBlock(pos, _.decrementY, _.decrementX)
  def bottomRightBlock(pos: GridPositionOLD): List[GridPositionOLD] = buildBlock(pos, _.decrementY, _.incrementX)

  private def buildLine(start: GridPositionOLD, step: GridPositionOLD => Option[GridPositionOLD]): List[GridPositionOLD] = {
    val lineBuff = ListBuffer[GridPositionOLD]()
    lineBuff += start

    @tailrec
    def rec(current: GridPositionOLD): List[GridPositionOLD] = step(current) match {
      case None => lineBuff.result()
      case Some(next) =>
        lineBuff += next
        rec(next)
    }
    rec(start)
  }

  private def getLast(start: GridPositionOLD, step: GridPositionOLD => Option[GridPositionOLD]): GridPositionOLD = {
    @tailrec
    def rec(current: GridPositionOLD): GridPositionOLD = step(current) match {
      case None => current
      case Some(next) => rec(next)
    }
    rec(start)
  }

  private def buildBlock(pos: GridPositionOLD, first: GridPositionOLD => Option[GridPositionOLD], second: GridPositionOLD => Option[GridPositionOLD]): List[GridPositionOLD] = {
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
