package models

import fr.njin.playoauth.common.domain.OauthResourceOwner
import org.joda.time.DateTime
import pk.auth.CBType
import pk.auth.CBType.TUser
import play.api.libs.json.Json

case class User(id:String,
                email: String,
                password: String,
                createdAt: DateTime) extends OauthResourceOwner

object User {
  private val addTpe = CBType.addTpe(TUser)

  implicit val reads = Json.reads[User]
  implicit val writes = Json.writes[User] transform addTpe
}
