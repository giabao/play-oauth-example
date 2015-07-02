package controllers

import javax.inject.{Singleton, Inject}
import cao.UserCao
import domain.Security
import models.{Signup, Signin}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import com.couchbase.client.java.query.consistency.ScanConsistency.REQUEST_PLUS
import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class Application @Inject() (userCao: UserCao, security: Security, val messagesApi: MessagesApi)
    extends Controller with I18nSupport {
  import Application._

  def index = security.Authenticated { implicit req =>
    Ok(views.html.index())
  }

  def signIn(back: Option[String]) = Action { implicit req =>
    Ok(views.html.signin(back, signInForm))
  }

  def doSignIn(back: Option[String]) = Action.async { implicit req =>
    val form = signInForm.bindFromRequest
    form.fold(
      f => Future successful BadRequest(views.html.signin(back, f)),
      d => userCao.byNameOrEmail(d.nameOrEmail).flatMap {
        case None =>
          Future successful BadRequest(views.html.signin(back, form.withGlobalError("error.user.not.found")))
        case Some(u) if !userCao.checkPw(u, d.password) =>
          Future successful BadRequest(views.html.signin(back, form.withGlobalError("error.credentials.not.match")))
        case Some(u) =>
          security.logIn(u, back.getOrElse(routes.Application.index.url))
      }
    )
  }

  def signUp(back: Option[String]) = Action { implicit req =>
    Ok(views.html.signup(back, signUpForm))
  }

  def doSignUp(back: Option[String]) = Action.async { implicit req =>
    val form = signUpForm.bindFromRequest
    form.fold(
      f => Future successful BadRequest(views.html.signup(back, f)),
      d => userCao.byEmail(d.email, REQUEST_PLUS).flatMap {
        case Some(_) =>
          Future successful BadRequest(views.html.signup(back, form.withGlobalError("error.user.exist")))
        case None =>
          userCao.create(d).flatMap(u =>
            security.logIn(u, back.getOrElse(routes.Application.index.url))
          )
      }
    )
  }

  def logout = security.Authenticated.async { req =>
    security.logOut(req.user)
  }
}

import play.api.data.Forms._
import pk.auth.Constraints._

object Application {
  val signInForm = Form(
    mapping(
      "nameOrEmail" -> nameOrEmail,
      "password" -> password
    )(Signin.apply)(Signin.unapply)
  )

  //TODO use? https://github.com/chrisnappin/play-recaptcha
  val signUpForm = Form(
    mapping(
      "username" -> username,
      "email" -> email,
      "password" -> password
    )(Signup.apply)(Signup.unapply)
  )
}
