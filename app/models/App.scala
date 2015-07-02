package models

import java.time.Instant

import fr.njin.playoauth.common.OAuth.{ResponseType, GrantType}
import fr.njin.playoauth.common.domain.OauthClient
import org.joda.time.DateTime
import pk.auth.CBType
import pk.auth.CBType.TApp
import play.api.libs.json.Json

case class App(id: String,
               ownerId: String,
               secret: String,
               name: String,
               description: String,
               uri: String,
               iconUri: Option[String],
               redirectUris: Option[Seq[String]],
               isWebApp: Boolean,
               isNativeApp: Boolean,
               createdAt: Instant) extends OauthClient {
  def authorized = true

  val redirectUri: Option[String] = redirectUris.flatMap(_.headOption)

  val allowedResponseType: Seq[String] =
    if (confidential) Seq(ResponseType.Token, ResponseType.Code)
    else Seq(ResponseType.Code)

  val allowedGrantType: Seq[String] = Seq(GrantType.AuthorizationCode, GrantType.ClientCredentials, GrantType.RefreshToken)

  /** @see http://tools.ietf.org/html/rfc6749#section-2.1 */
  def confidential = isWebApp || isNativeApp

  def icon = iconUri.getOrElse(controllers.routes.Assets.at("images/default.png").url)
}

object App {
  private val addTpe = CBType.addTpe(TApp)

  implicit val reads = Json.reads[App]
  implicit val writes = Json.writes[App] transform addTpe
}