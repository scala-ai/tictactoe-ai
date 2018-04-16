package de.ai.scala.aiClient.model.format

import de.ai.scala.aiClient.model.Coordinate
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import play.api.libs.json.Format
import play.api.libs.json.JsResult


object CoordinateFormat extends Format[Coordinate] {
  override def writes(o: Coordinate): JsValue = {
    Json.obj(
      "x" -> o.x,
      "y" -> o.y,
    )
  }

  override def reads(json: JsValue): JsResult[Coordinate] = {
    for {
      x <- json.\("x").validate[Int]
      y <- json.\("y").validate[Int]
    } yield {
      Coordinate(x, y)
    }
  }
}
