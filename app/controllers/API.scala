package controllers

import javax.inject.{Inject, Singleton}
import cao.{UserCao, TokenCao}
import fr.njin.playoauth.Utils
import fr.njin.playoauth.rs.Oauth2Resource
import models.{User, AuthToken}
import play.api.mvc.Controller
import models.User._
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class API @Inject() (tokenRepo: TokenCao,
                     userRepo: UserCao)
  extends Controller with Oauth2Resource[AuthToken, User] {

  private val userInfo = toUserInfo(Utils.parseBearer, tokenRepo.find, userRepo.find)

  /** A example of resource protection */
  def user = ScopedAction(Seq("basic"), userInfo) { req =>
      Ok(Json.toJson(req.user))
  }
}
