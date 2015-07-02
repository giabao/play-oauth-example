package controllers

import javax.inject.{Inject, Singleton}
import cao.{UserCao, TokenCao, CodeCao, AppCao}
import fr.njin.playoauth.Utils
import fr.njin.playoauth.as.OauthError
import fr.njin.playoauth.as.endpoints.SecretKeyClientAuthentication
import fr.njin.playoauth.common.OAuth
import models.{AuthToken, _}
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, _}
import fr.njin.playoauth.as.endpoints
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class Token @Inject() (appCao: AppCao, userCao: UserCao,
                       endpoint: TokenEndpoint) extends Controller {
  /**
   * Token endpoint call
   *
   * @return see [[fr.njin.playoauth.as.endpoints.Token.token]]
   */
  def token = Action.async(parse.urlFormEncoded.map(new AnyContentAsFormUrlEncoded(_)))(
    endpoint.token(
      (u,p) => userCao.byNameOrEmail(u).map(_.filter(userCao.checkPw(_, p))),
      app => userCao.find(app.ownerId)
    )
  )

  /**
   * Token info
   *
   * Send a token as json to the client.
   * Use http basic authentication to authenticate the client.
   *
   * @param value value of the token
   * @return see [[fr.njin.playoauth.as.endpoints.Token.info]]
   */
  def info(value: String) = Action.async {
    endpoint.info(value, clientAuthenticate, infoOk)
  }

  private def clientAuthenticate(req: RequestHeader): Future[Option[App]] =
    Utils.parseBasicAuth(req).map{
      case (id, secret) => appCao.find(id).map(_.filter(_.secret == secret))
    }.getOrElse(Future successful None)

  private def infoOk(token: AuthToken) = Future successful Ok(Json.toJson(token))
}

@Singleton
class TokenEndpoint @Inject() (appCao: AppCao, codeCao: CodeCao, tokenCao: TokenCao,
                               implicit val messagesApi: MessagesApi)
    extends endpoints.Token[App, AuthCode, User, AuthToken] with SecretKeyClientAuthentication[App] {

  def clientRepository = appCao
  def codeRepository = codeCao
  def tokenFactory = tokenCao
  def tokenRepository = tokenCao
  def supportedGrantType = OAuth.GrantType.All

  def authenticate(id: String, secret: String): Future[Either[Option[App], OauthError]] =
    clientRepository.find(id).map(_.fold[Either[Option[App], OauthError]](Left(None)) { app =>
      if(app.secret == secret)
        Left(Some(app))
      else
        Right(OauthError.invalidClientError(Some(Messages(OAuth.ErrorClientCredentialsDontMatch))))
    })
}
