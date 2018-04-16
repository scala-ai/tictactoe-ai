package de.ai.htwg.tictactoe.gameLogic.model.format

import de.ai.htwg.tictactoe.gameLogic.model.GridPosition
import play.api.libs.json.Format
import play.api.libs.json.JsValue
import play.api.libs.json.JsResult
import play.api.libs.json.Json

object GridPositionFormat extends Format[GridPosition] {
  override def writes(o: GridPosition): JsValue = {
    Json.obj(
      "x" -> o.x,
      "y" -> o.y,
    )
  }

  override def reads(json: JsValue): JsResult[GridPosition] = {
    for {
      x <- json.\("x").validate[Int]
      y <- json.\("y").validate[Int]
      if GridPosition.inRange(x)
      if GridPosition.inRange(y)
    } yield {
      GridPosition(x, y)
    }
  }
}
