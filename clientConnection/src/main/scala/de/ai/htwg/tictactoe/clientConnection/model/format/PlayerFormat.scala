package de.ai.htwg.tictactoe.clientConnection.model.format

import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.Player
import de.ai.htwg.tictactoe.clientConnection.model.Player
import play.api.libs.json.JsValue
import play.api.libs.json.JsResult
import play.api.libs.json.Format
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import play.api.libs.json.JsonValidationError

object PlayerFormat extends Format[Player] {

  private val crossLit = "X"
  private val circleLit = "O"
  override def writes(o: Player): JsValue = {
    o match {
      case Player.Cross => JsString(crossLit)
      case Player.Circle => JsString(circleLit)
    }
  }

  override def reads(json: JsValue): JsResult[Player] = {
    json match {
      case JsString(this.crossLit) => JsSuccess(Player.Cross)
      case JsString(this.circleLit) => JsSuccess(Player.Circle)
      case e => JsError(JsonValidationError(s"Not a valid player String: $e"))
    }
  }
}
