package domain

import java.util.UUID
import javax.inject.{Inject, Singleton}
import cao.UserCao
import controllers.routes
import models.User
import play.api.libs.Crypto
import play.api.mvc.Security.AuthenticatedRequest
import play.api.mvc._
import scala.concurrent.Future
import scala.language.reflectiveCalls
import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class Security @Inject() (userCao: UserCao) extends AuthenticationHandler {
  def onUnauthenticated(request: RequestHeader): Future[Result] =
    Future successful Results.Redirect(routes.Application.signIn(Option(request.uri)).url)

  def userInfo(request: RequestHeader): Future[Option[User]] =
    (for {
      sessionId <- request.session.get("sessionId")
      email <- request.session.get("email")
      password <- request.session.get("password")
    } yield userCao
        .byEmail(Crypto.decryptAES(email))
        .map(_.filter(u => u.password == Crypto.decryptAES(password)))
      ).getOrElse(Future successful None)

  object Authenticated extends ActionBuilder[({type R[A] = AuthenticatedRequest[A, User]})#R] {
    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A, User]) => Future[Result]): Future[Result] = {
      userInfo(request).flatMap(_.fold(onUnauthenticated(request))(u => block(new AuthenticatedRequest(u, request))))
    }
  }
}

trait AuthenticationHandler {
  def logIn(user:User, redirectUrl: String): Future[Result] = {
    Future.successful(Results.Redirect(redirectUrl).withNewSession
      .withSession(
        "sessionId" -> UUID.randomUUID().toString,
        "email" -> Crypto.encryptAES(user.email),
        "password" -> Crypto.encryptAES(user.password)
      )
    )
  }

  def logOut(user:User): Future[Result] = {
    Future.successful(Results.Redirect(routes.Application.signIn()).withNewSession)
  }
}
