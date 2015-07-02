package controllers

import javax.inject.{Inject, Singleton}
import domain.Security
import play.api.mvc.Controller

@Singleton
class Users @Inject() (security: Security) extends Controller {
  def profile = security.Authenticated.async { implicit req =>
    TODO(req)
  }

  def apps = security.Authenticated.async { implicit req =>
    TODO(req)
  }
}
