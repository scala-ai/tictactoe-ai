package de.ai.htwg.tictactoe.playerClient

import scala.collection.JavaConverters

import de.ai.htwg.tictactoe.clientConnection.model.Player
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


object GameUiStage {
  val defaultAppWidth: Double = 500
  val defaultAppHeight: Double = 500
}

class GameUiStage(name: String, dimensions: Int, startingPlayer: Player) extends Stage {
  title = name

  scene = new Scene(GameUiStage.defaultAppWidth, GameUiStage.defaultAppHeight) {
    val anchor = new AnchorPane
    root = anchor

    private var current = startingPlayer
    private def handleMouseClickedEvent(me: MouseEvent): Unit = {
      val cellWidth = anchor.width.value / dimensions
      val cellHeight = anchor.height.value / dimensions

      val x = math.floor(me.x / cellWidth).toInt
      val y = math.floor(me.y / cellHeight).toInt


      current match {
        case Player.Cross => addX(x, y)
        case Player.Circle => addO(x, y)
      }

      current = Player.other(current)

    }

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

    def addElem(x: Int, y: Int, elem: Node, offset: Double): Unit = {
      anchor.children += elem

      def resize: Unit = {
        val minSize = Math.min(anchor.width.value, anchor.height.value) / dimensions
        val scale = minSize / 20
        elem.scaleX = scale
        elem.scaleY = scale

        AnchorPane.setTopAnchor(elem, anchor.height.value / dimensions * (y + 0.5) - offset)
        AnchorPane.setLeftAnchor(elem, anchor.width.value / dimensions * (x + 0.5) - offset)
      }
      anchor.width.onChange(resize)
      anchor.height.onChange(resize)
      resize
    }

    def addO(x: Int, y: Int): Unit = {

      val circleRadius = 7.0

      val elem = new Circle {
        stroke = Green
        radius = circleRadius
        fill = Transparent

        strokeWidth = 2
      }

      addElem(x, y, elem, circleRadius)
    }

    def addX(x: Int, y: Int): Unit = {
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

      addElem(x, y, elem, size / 2.0)
    }


  }
}
