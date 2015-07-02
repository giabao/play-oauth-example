package models

import fr.njin.playoauth.common.domain.OauthPermission
import fr.njin.playoauth.common.request.AuthzRequest
import pk.auth.CBType
import pk.auth.CBType.TPermission
import play.api.libs.json.Json

case class Permission(clientId: String,
                      redirectUri: Option[String],
                      scopes: Option[Seq[String]]) extends OauthPermission {
  def authorized(req: AuthzRequest): Boolean =
    req.clientId == clientId &&
      req.redirectUri == redirectUri &&
      req.scopes.forall(_.forall(scope => this.scopes.exists(_.contains(scope))))
}

object Permission {
  private val addTpe = CBType.addTpe(TPermission)

  implicit val reads = Json.reads[Permission]
  implicit val writes = Json.writes[Permission] transform addTpe
}

