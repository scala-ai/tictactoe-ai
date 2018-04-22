package de.ai.htwg.tictactoe.clientConnection.model.format

import de.ai.htwg.tictactoe.clientConnection.model.GridPosition
import play.api.libs.json.Format
import play.api.libs.json.JsValue
import play.api.libs.json.JsResult
import play.api.libs.json.Json
import play.api.libs.json.Writes

object GridPositionFormat extends Writes[GridPosition] {
  def apply(dimensions: Int): GridPositionFormat = new GridPositionFormat(GridPosition(dimensions))
  override def writes(o: GridPosition): JsValue = {
    Json.obj(
      "x" -> o.x,
      "y" -> o.y,
    )
  }
}

class GridPositionFormat(posBuilder: GridPosition.Builder) extends Format[GridPosition] {
  override def writes(o: GridPosition): JsValue = GridPositionFormat.writes(o)
  override def reads(json: JsValue): JsResult[GridPosition] = {
    for {
      x <- json.\("x").validate[Int]
      y <- json.\("y").validate[Int]
      if posBuilder.inRange(x)
      if posBuilder.inRange(y)
    } yield {
      posBuilder(x, y)
    }
  }
}
