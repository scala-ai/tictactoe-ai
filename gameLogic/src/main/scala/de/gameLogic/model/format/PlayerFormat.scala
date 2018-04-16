package de.gameLogic.model.format


import de.gameLogic.model.Player
import play.api.libs.json.JsValue
import play.api.libs.json.JsResult
import play.api.libs.json.Format
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import play.api.libs.json.JsonValidationError

object PlayerFormat extends Format[Player] {

  private val p1Lit = "X"
  private val p2Lit = "O"
  override def writes(o: Player): JsValue = {
    o match {
      case Player.One => JsString(p1Lit)
      case Player.Two => JsString(p2Lit)
    }
  }

  override def reads(json: JsValue): JsResult[Player] = {
    json match {
      case JsString(this.p1Lit) => JsSuccess(Player.One)
      case JsString(this.p2Lit) => JsSuccess(Player.Two)
      case e => JsError(JsonValidationError(s"Not a valid player String: $e"))
    }
  }
}
