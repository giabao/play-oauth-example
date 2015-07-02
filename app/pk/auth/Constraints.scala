package pk.auth

import play.api.data.Mapping
import play.api.data.format.Formats._
import play.api.data.validation.{Valid, ValidationError, Invalid, Constraint}
import play.api.data.validation.Constraints._
import play.api.data.Forms._
import play.api.libs.json.Reads

object Constraints {
  /** password must:
    * 1. length: 6-20
    * 2. only contains alphanumeric or special chars [@#$%]
    * 3. must contains at least one alphabet & one digit or special char */
  private val passwordRegex = """((?=.*[a-zA-Z])(?=.*[@#$%0-9]).{6,20})""".r

  private val usernameRegex = """^[a-zA-Z][a-zA-Z0-9]{4,40}$""".r

  def jsPassword(implicit reads: Reads[String]): Reads[String] = Reads.pattern(passwordRegex, "error.password")

  def jsUsername(implicit reads: Reads[String]): Reads[String] = Reads.pattern(usernameRegex, "error.username")

  def pattern(regex: => scala.util.matching.Regex, error: String = "pattern") =
    Constraint[String](s"constraint.$error") { e =>
      if (e == null || e.trim.isEmpty) Invalid(ValidationError(s"error.$error"))
      else regex.findFirstMatchIn(e)
        .map(_ => Valid)
        .getOrElse(Invalid(ValidationError(s"error.$error")))
    }

  val validUsername = pattern(usernameRegex, "username")

  val password: Mapping[String] = of[String] verifying pattern(passwordRegex, "password")
  val username: Mapping[String] = of[String] verifying validUsername

  implicit class ConstraintEx[T](val underlying: Constraint[T]) extends AnyVal {
    def or(other: Constraint[T]): Constraint[T] = Constraint { t =>
      underlying(t) match {
        case Valid => Valid
        case Invalid(_) => other(t)
      }
    }
  }

  val nameOrEmail: Mapping[String] = of[String] verifying (validUsername or emailAddress)
}