package controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}
import cao._
import domain.Security
import fr.njin.playoauth.as.endpoints.Constraints._
import fr.njin.playoauth.as.endpoints.Requests._
import fr.njin.playoauth.common.OAuth
import fr.njin.playoauth.common.request.AuthzRequest
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.uuid
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Result, RequestHeader, Action, Controller}
import fr.njin.playoauth.as.endpoints
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Authorization {
  case class PermissionForm(appId: String,
                            decision: Boolean,
                            scope: Option[Seq[String]],
                            redirectUri: Option[String],
                            state: Option[String])

  val permissionForm =  Form (
    mapping(
      "appId" -> uuid.transform(_.toString, UUID.fromString),
      "decision" -> boolean,
      "scope" -> optional(of[Seq[String]](scopeFormatter)),
      "redirectUri" -> optional(text.verifying(uri)),
      "state" -> optional(text)
    )(PermissionForm.apply)(PermissionForm.unapply)
  )
}

@Singleton
class Authorization @Inject()(implicit val messagesApi: MessagesApi,
                              val userCao: UserCao,
                              security: Security,
                              endpoint: AuthorizationEndpoint,
                              appCao: AppCao, permissionCao: PermissionCao)
    extends Controller with I18nSupport{
  import Authorization._

  /** If unauthorized, we show a permission form to the user */
  private def onUnauthorized(owner: User)(ar: AuthzRequest, app: App): RequestHeader => Future[Result] =
    implicit req => {
      val form = permissionForm.fill(PermissionForm(app.id, decision = false, ar.scopes, ar.redirectUri, ar.state))
      Future successful Ok(views.html.authorize(app, form, owner))
    }

  /**
   * Authorization endpoint call
   * @return
   */
  def authz() = Action.async(parse.empty) {
    endpoint.performAuthorize(
      security.userInfo,
      (ar, c) => security.onUnauthenticated,
      onUnauthorized
    )
  }

  /**
   * Create the permission then redirect to [[authz]]
   * to finish the authorization process.
   * @return
   */
  // FIXME The client can steal a permission by making a direct post
  // @bathily - sao ???c nh?? :(
  def authorize = security.Authenticated.async { implicit req =>
    permissionForm.bindFromRequest.fold(f => Future.successful(BadRequest), pForm => {
      appCao.find(pForm.appId).flatMap(_.fold(Future.successful(NotFound(""))) { app =>
        val permission = Permission(app.id, pForm.redirectUri, pForm.scope)
        permissionCao.setT(req.user.id, app.id, permission).map { p =>
          Redirect(routes.Authorization.authz.url,
            AuthzRequest(OAuth.ResponseType.Code,
              app.id,
              pForm.redirectUri,
              pForm.scope,
              pForm.state
            )
          )
        }
      })
    })
  }
}

@Singleton
class AuthorizationEndpoint @Inject()(val clientRepository: AppCao,
                                      val codeFactory: CodeCao,
                                      val tokenFactory: TokenCao,
                                      val permissions: PermissionCao,
                                      implicit val messagesApi: MessagesApi)
    extends endpoints.Authorization[App, AuthCode, User, Permission, AuthToken]
{
  def supportedResponseType = OAuth.ResponseType.All

  def allScopes = Seq("profile")
}
