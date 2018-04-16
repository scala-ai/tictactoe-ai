package de.ai.htwg.tictactoe.aiClient.model.format

import de.ai.htwg.tictactoe.aiClient.model.Coordinate
import de.ai.htwg.tictactoe.aiClient.model.Field
import de.ai.htwg.tictactoe.aiClient.model.Playground
import play.api.libs.json.JsObject
import play.api.libs.json.JsResult
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.OFormat
import play.api.libs.json.Reads


object PlaygroundFormat extends OFormat[Playground] {

  private val posLit = "pos"
  private val playerLit = "player"

  private def writeGameFieldEntry(pair: (Coordinate, Field)): JsObject = {
    val (pos, p) = pair
    Json.obj(
      posLit -> CoordinateFormat.writes(pos),
      playerLit -> FieldFormat.writes(p)
    )
  }

  private def readGameFieldEntry: Reads[(Coordinate, Field)] = Reads { js =>
    for {
      pos <- js.\(posLit).validate(CoordinateFormat)
      p <- js.\(playerLit).validate(FieldFormat)
    } yield {
      pos -> p
    }
  }


  private val finishedLit = "isFinished"
  private val currentPlayerLit = "currentPlayer"
  private val fieldLit = "field"
  private val dimensions = "dimensions"

  override def writes(o: Playground): JsObject = {

    Json.obj(
      finishedLit -> false,
      fieldLit -> o.mapToCoordinate.collect { case p @ (_, Field.Circle | Field.Cross) => writeGameFieldEntry(p) },
      dimensions -> o.dimensions,
    )
  }


  override def reads(json: JsValue): JsResult[Playground] = {
    for {
      finished <- json.\(finishedLit).validate[Boolean]
      currentPlayer <- json.\(currentPlayerLit).validate(FieldFormat)
      field <- json.\(fieldLit).validate(Reads.list(readGameFieldEntry))
      dimensions <- json.\(dimensions).validate[Int]
    } yield {
      Playground.fromCoordinateMap(field.toMap, dimensions)
    }
  }
}
