package models

import java.time.Instant
import fr.njin.playoauth.common.OAuth
import fr.njin.playoauth.common.domain.OauthToken
import pk.auth.CBType
import pk.auth.CBType.TToken
import play.api.libs.json.Json
import scala.concurrent.duration.FiniteDuration

case class AuthToken(value: String,
                     ownerId: String,
                     clientId: String,
                     tokenType: String,
                     issueAt: Instant = Instant.now,
                     expiresIn: FiniteDuration = OAuth.MaximumLifetime,
                     scopes: Option[Seq[String]] = None,
                     revokeAt: Option[Instant] = None,
                     refreshToken: Option[String] = None) extends OauthToken {
  def revoked = revokeAt.isDefined
}

object AuthToken {
  private val addTpe = CBType.addTpe(TToken)

  implicit val reads = Json.reads[AuthToken]
  implicit val writes = Json.writes[AuthToken] transform addTpe
}
