package de.ai.htwg.tictactoe.clientConnection.fxUI

import scala.collection.JavaConverters
import scala.concurrent.Future
import scala.concurrent.Promise

import de.ai.htwg.tictactoe.clientConnection.model.GameField
import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import de.ai.htwg.tictactoe.clientConnection.model.Player
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.ReadOnlyDoubleProperty
import scalafx.geometry.Orientation
import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.control.Separator
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.AnchorPane
import scalafx.scene.paint.Color.Green
import scalafx.scene.paint.Color.Red
import scalafx.scene.paint.Color.Transparent
import scalafx.scene.shape.Circle
import scalafx.scene.shape.Polygon
import scalafx.stage.Stage


object GameUiStage {
  val defaultAppSize: Double = 500
  val minAppSize: Double = 150

  val borderSize: Double = 50

  trait UIElem {
    def set(): Unit
    def clear(): Unit
  }

  val onMouseClickedDefault: GridPosition => Unit = _ => ()


  def apply(name: String, dimensions: Int): Future[GameUiStage] = {
    val p = Promise[GameUiStage]()
    Platform.runLater(p.success(new GameUiStage(name, dimensions)))

    p.future
  }

}

class GameUiStage private(name: String, dimensions: Int) {

  private var onMouseClicked: GridPosition => Unit = GameUiStage.onMouseClickedDefault

  private val anchor = new AnchorPane

  private val stage = new Stage {
    minHeight = GameUiStage.minAppSize
    minWidth = GameUiStage.minAppSize
    title = name
    scene = new Scene(GameUiStage.defaultAppSize, GameUiStage.defaultAppSize) {
      root = anchor

      onMouseClicked = handleMouseClickedEvent _

      def sizeElement(mod: Double, size: ReadOnlyDoubleProperty)(setAnchor: Double => Unit): Unit = {
        def calc(size: Double): Double = mod * (size - GameUiStage.borderSize) + GameUiStage.borderSize
        setAnchor(calc(GameUiStage.defaultAppSize))
        size.onChange {
          setAnchor(calc(size.value))
        }
      }

      def createVerticalLine(mod: Double): Unit = {
        val line: Separator = Separator(Orientation.Vertical)
        anchor.children += line
        AnchorPane.setBottomAnchor(line, 0)
        AnchorPane.setTopAnchor(line, 0)
        sizeElement(mod, anchor.width) { width =>
          AnchorPane.setLeftAnchor(line, width)
        }
      }

      def createHorizontalLine(mod: Double): Unit = {
        val line = Separator(Orientation.Horizontal)
        anchor.children += line
        AnchorPane.setLeftAnchor(line, 0)
        AnchorPane.setRightAnchor(line, 0)
        sizeElement(mod, anchor.height) { height =>
          AnchorPane.setTopAnchor(line, height)
        }
      }

      def createLine(
          mod: Double,
          orientation: Orientation,
          size: ReadOnlyDoubleProperty,
          dynAnchor: (Node, Double) => Unit,
          setAchors: (Node, Double) => Unit*
      ): Unit = {
        val line = Separator(orientation)
        anchor.children += line
        setAchors.foreach(_ (line, 0))
        sizeElement(mod, size) { pos =>
          dynAnchor(line, pos)
        }
      }

      def createLabel(i: Int, size: ReadOnlyDoubleProperty, setAnchor: (Node, Double) => Unit, dynAnchor: (Node, Double) => Unit): Unit = {
        val label = new Label(s"$i") {
          alignment = Pos.Center
          prefWidth = 20.0
          scaleX = 3
          scaleY = 3
        }
        anchor.children += label
        setAnchor(label, 10.0)
        val mod = (i.toDouble + 0.5) / dimensions.toDouble
        sizeElement(mod, size) { pos =>
          dynAnchor(label, pos)
        }
      }

      for (i <- 0 until dimensions) {
        createLabel(i, anchor.height, AnchorPane.setLeftAnchor, AnchorPane.setTopAnchor)
        createLabel(i, anchor.width, AnchorPane.setTopAnchor, AnchorPane.setLeftAnchor)
      }

      for (i <- 0 to dimensions + 1) {
        val mod = i.toDouble / dimensions.toDouble
        createLine(mod, Orientation.Vertical, anchor.width, AnchorPane.setLeftAnchor, AnchorPane.setBottomAnchor, AnchorPane.setTopAnchor)
        createLine(mod, Orientation.Horizontal, anchor.height, AnchorPane.setTopAnchor, AnchorPane.setLeftAnchor, AnchorPane.setRightAnchor)
      }

    }
  }

  anchor.width.onChange(resizeAll())
  anchor.height.onChange(resizeAll())
  Platform.runLater(resizeAll())


  private def handleMouseClickedEvent(me: MouseEvent): Unit = {
    val cellWidth = (anchor.width.value - GameUiStage.borderSize) / dimensions
    val cellHeight = (anchor.height.value - GameUiStage.borderSize) / dimensions

    val x = math.floor((me.x - GameUiStage.borderSize) / cellWidth).toInt
    val y = math.floor((me.y - GameUiStage.borderSize) / cellHeight).toInt

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
    def resize(unadjustedWidth: Double, unadjustedHeight: Double): Unit = {
      val width = unadjustedWidth - GameUiStage.borderSize
      val height = unadjustedHeight - GameUiStage.borderSize
      val minSize = Math.min(width, height) / dimensions
      val scale = minSize / 20
      elem.scaleX = scale
      elem.scaleY = scale

      AnchorPane.setTopAnchor(elem, height / dimensions * (y + 0.5) - offset + GameUiStage.borderSize)
      AnchorPane.setLeftAnchor(elem, width / dimensions * (x + 0.5) - offset + GameUiStage.borderSize)
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
    Platform.runLater {
      stage.close()
    }
  }

  def setOnMouseClicked(onMouseClicked: GridPosition => Unit): Unit = {
    Platform.runLater {
      this.onMouseClicked = onMouseClicked
    }
  }

  def printField(field: GameField): Unit = {
    clear()
    field.foreach {
      case (pos, Player.Cross) => cross(pos).set()
      case (pos, Player.Circle) => circle(pos).set()
    }
  }

}
