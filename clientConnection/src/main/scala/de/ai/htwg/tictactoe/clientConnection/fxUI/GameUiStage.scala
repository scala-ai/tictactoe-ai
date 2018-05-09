package de.ai.htwg.tictactoe.clientConnection.fxUI

import scala.collection.JavaConverters
import scala.concurrent.Promise
import scala.concurrent.Future

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import scalafx.geometry.Orientation
import scalafx.scene.Node
import scalafx.scene.Scene
import scalafx.scene.control.Separator
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.AnchorPane
import scalafx.scene.paint.Color.Red
import scalafx.scene.paint.Color.Transparent
import scalafx.scene.paint.Color.Green
import scalafx.scene.shape.Polygon
import scalafx.scene.shape.Circle
import scalafx.stage.Stage
import scalafx.Includes._
import scalafx.application.Platform


object GameUiStage {
  val defaultAppWidth: Double = 500
  val defaultAppHeight: Double = 500

  trait UIElem {
    def set(): Unit
    def clear(): Unit
  }

  def apply(name: String, dimensions: Int, onMouseClicked: GridPosition => Unit): Future[GameUiStage] = {
    val p = Promise[GameUiStage]()
    Platform.runLater(p.success(new GameUiStage(name, dimensions, onMouseClicked)))

    p.future
  }

}

class GameUiStage private(name: String, dimensions: Int, onMouseClicked: GridPosition => Unit) {

  private val anchor = new AnchorPane

  private val stage = new Stage {
    title = name
    scene = new Scene(GameUiStage.defaultAppWidth, GameUiStage.defaultAppHeight) {
      root = anchor

      onMouseClicked = handleMouseClickedEvent _


      for (i <- 0 to dimensions + 1) {
        val mod = i.toDouble / dimensions.toDouble
        val line = Separator(Orientation.Vertical)
        anchor.children += line
        AnchorPane.setBottomAnchor(line, 0)
        AnchorPane.setTopAnchor(line, 0)
        AnchorPane.setLeftAnchor(line, mod * GameUiStage.defaultAppWidth)
        anchor.width.onChange {
          AnchorPane.setLeftAnchor(line, mod * anchor.width.value)
        }
      }

      for (i <- 0 to dimensions + 1) {
        val mod = i.toDouble / dimensions.toDouble
        val line = Separator(Orientation.Horizontal)
        anchor.children += line
        AnchorPane.setLeftAnchor(line, 0)
        AnchorPane.setRightAnchor(line, 0)
        AnchorPane.setTopAnchor(line, mod * GameUiStage.defaultAppHeight)
        anchor.height.onChange {
          AnchorPane.setTopAnchor(line, mod * anchor.height.value)
        }
      }


    }
  }

  anchor.width.onChange(resizeAll())
  anchor.height.onChange(resizeAll())
  Platform.runLater(resizeAll())


  private def handleMouseClickedEvent(me: MouseEvent): Unit = {
    val cellWidth = anchor.width.value / dimensions
    val cellHeight = anchor.height.value / dimensions

    val x = math.floor(me.x / cellWidth).toInt
    val y = math.floor(me.y / cellHeight).toInt

    val pos = GridPosition(x, y)
    onMouseClicked(pos)
  }

  private case class UIElemImpl(elem: Node, resize: (Double, Double) => Unit) extends GameUiStage.UIElem {
    override def set(): Unit = {
      Platform.runLater {
        // just to make sure the element is not already added
        anchor.children -= elem
        anchor.children += elem
      }
    }
    override def clear(): Unit = {
      Platform.runLater {
        anchor.children -= elem
      }
    }
  }

  private def createElem(x: Int, y: Int, elem: Node, offset: Double): UIElemImpl = {
    def resize(width: Double, height: Double): Unit = {
      val minSize = Math.min(width, height) / dimensions
      val scale = minSize / 20
      elem.scaleX = scale
      elem.scaleY = scale

      AnchorPane.setTopAnchor(elem, height / dimensions * (y + 0.5) - offset)
      AnchorPane.setLeftAnchor(elem, width / dimensions * (x + 0.5) - offset)
    }

    UIElemImpl(elem, resize)
  }

  private def createCircle(x: Int, y: Int): UIElemImpl = {

    val circleRadius = 7.0

    val elem = new Circle {
      stroke = Green
      radius = circleRadius
      fill = Transparent

      strokeWidth = 2
    }

    createElem(x, y, elem, circleRadius)
  }

  private def createCross(x: Int, y: Int): UIElemImpl = {
    val size = 16
    val lineWidth = 3

    val elem = new Polygon() {
      private val step = (size - lineWidth) / 2.0
      private val start = 0.0
      private val end = start + size
      private val startCross = start + step
      private val endCross = end - step
      val pointsList = List(
        // 0
        start, startCross,
        // 1
        start, endCross,
        // 2
        startCross, endCross,
        // 3
        startCross, end,
        // 4
        endCross, end,
        // 5
        endCross, endCross,
        // 6
        end, endCross,
        // 7
        end, startCross,
        // 8
        endCross, startCross,
        // 9
        endCross, start,
        // 10
        startCross, start,
        // 11
        startCross, startCross,
      )
      points.setAll(JavaConverters.seqAsJavaList(pointsList.map(double2Double)))
      rotate = 45
      fill = Red
    }

    createElem(x, y, elem, size / 2.0)
  }

  private val (circles, crosses) = {
    val (circles, crosses) = (for {
      x <- 0 until dimensions
      y <- 0 until dimensions
    } yield {
      val pos = GridPosition(x, y)
      (
        pos -> createCircle(x, y),
        pos -> createCross(x, y),
      )
    }).unzip

    (circles.toMap, crosses.toMap)
  }

  private def resizeAll(): Unit = {
    val width = anchor.width.value
    val height = anchor.height.value
    circles.foreach { case (_, uiElem) =>
      uiElem.resize(width, height)
    }
    crosses.foreach { case (_, uiElem) =>
      uiElem.resize(width, height)
    }
  }

  def clear(): Unit = {
    circles.foreach { case (_, uiElem) =>
      uiElem.clear()
    }
    crosses.foreach { case (_, uiElem) =>
      uiElem.clear()
    }
  }
  def circle(pos: GridPosition): GameUiStage.UIElem = circles(pos)
  def cross(pos: GridPosition): GameUiStage.UIElem = crosses(pos)

  def show(): Unit = {
    Platform.runLater(
      stage.show()
    )
  }

  def stop(): Unit = {
    Platform.runLater{
      stage.close()
    }
  }


}
