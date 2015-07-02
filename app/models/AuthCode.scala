package models

import java.time.Instant
import fr.njin.playoauth.common.OAuth
import fr.njin.playoauth.common.domain.OauthCode
import pk.auth.CBType
import pk.auth.CBType.TCode
import play.api.libs.json.Json
import scala.concurrent.duration.FiniteDuration

case class AuthCode(value: String,
                    ownerId: String,
                    clientId: String,
                    issueAt: Instant = Instant.now,
                    expiresIn: FiniteDuration = OAuth.MaximumLifetime,
                    scopes: Option[Seq[String]] = None,
                    redirectUri: Option[String] = None) extends OauthCode {
  def revoked = false
}

object AuthCode {
  private val addTpe = CBType.addTpe(TCode)

  implicit val reads = Json.reads[AuthCode]
  implicit val writes = Json.writes[AuthCode] transform addTpe
}
