package de.ai.scala.tictactoe.model.format

import de.ai.scala.tictactoe.model.Field
import play.api.libs.json.Format
import play.api.libs.json.JsValue
import play.api.libs.json.JsString
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import play.api.libs.json.JsonValidationError


object FieldFormat extends Format[Field] {

  private val p1Lit = "X"
  private val p2Lit = "O"
  override def writes(o: Field): JsValue = {
    o match {
      case Field.Cross => JsString(p1Lit)
      case Field.Circle => JsString(p2Lit)
    }
  }

  override def reads(json: JsValue): JsResult[Field] = {
    json match {
      case JsString(this.p1Lit) => JsSuccess(Field.Cross)
      case JsString(this.p2Lit) => JsSuccess(Field.Circle)
      case e => JsError(JsonValidationError(s"Not a valid player String: $e"))
    }
  }
}
