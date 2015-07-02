package models

import pk.auth.Constraints
import play.api.libs.json._
import play.api.libs.json.Reads
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Signin(nameOrEmail: String, password: String)
object Signin {
  implicit val reads: Reads[Signin] = (
      (__ \ 'email).read[String](email) ~
      (__ \ 'password).read[String](Constraints.jsPassword)
    )(Signin.apply _)
}

case class Signup(username: String, email: String, password: String)
object Signup {
  implicit val reads: Reads[Signup] = (
      (__ \ 'username).read[String](Constraints.jsUsername) ~
      (__ \ 'email).read[String](email) ~
      (__ \ 'password).read[String](Constraints.jsPassword)
    )(Signup.apply _)
}
