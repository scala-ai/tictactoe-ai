package de.ai.htwg.tictactoe.clientConnection.model.format

import de.ai.htwg.tictactoe.clientConnection.model.GridPositionOLD
import play.api.libs.json.Format
import play.api.libs.json.JsValue
import play.api.libs.json.JsResult
import play.api.libs.json.Json
import play.api.libs.json.Writes

object GridPositionFormat extends Writes[GridPositionOLD] {
  def apply(dimensions: Int): GridPositionFormat = new GridPositionFormat(GridPositionOLD(dimensions))
  override def writes(o: GridPositionOLD): JsValue = {
    Json.obj(
      "x" -> o.x,
      "y" -> o.y,
    )
  }
}

class GridPositionFormat(posBuilder: GridPositionOLD.Builder) extends Format[GridPositionOLD] {
  override def writes(o: GridPositionOLD): JsValue = GridPositionFormat.writes(o)
  override def reads(json: JsValue): JsResult[GridPositionOLD] = {
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
