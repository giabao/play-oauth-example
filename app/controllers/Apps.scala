package controllers

import javax.inject.{Inject, Singleton}
import cao.AppCao
import domain.Security
import fr.njin.playoauth.as.endpoints.Constraints._
import fr.njin.playoauth.as.endpoints.Requests._
import models.{App, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Security.AuthenticatedRequest
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Apps {
  case class AppForm(name: String,
                     description: String,
                     uri: String,
                     iconUri: Option[String],
                     redirectUris: Option[Seq[String]],
                     isWebApp: Boolean,
                     isNativeApp: Boolean)

  object AppForm {
    def apply(app: App): AppForm = AppForm(
      app.name,
      app.description,
      app.uri,
      app.iconUri,
      app.redirectUris,
      app.isWebApp,
      app.isNativeApp
    )
  }

  val appForm = Form(
    mapping (
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "uri" -> nonEmptyText.verifying(uri),
      "iconUri" -> optional(text.verifying(uri)),
      "redirectUris" -> optional(of[Seq[String]](urisFormatter).verifying(uris)),
      "isWebApp" -> boolean,
      "isNativeApp" -> boolean
    )(AppForm.apply)(AppForm.unapply).verifying("error.redirectUri.required", app => {
      !(app.isWebApp || app.isNativeApp) || app.redirectUris.exists(_.nonEmpty)
    })
  )
}

@Singleton
class Apps @Inject() (val messagesApi: MessagesApi,
                      security: Security, appCao: AppCao) extends Controller with I18nSupport {
  import Apps._

  def WithApp(id: String)(action: App => AuthenticatedRequest[_, User] => Future[Result]) =
    security.Authenticated.async { implicit req =>
      appCao.find(id).flatMap(
        _.fold(Future successful NotFound(views.html.apps.notfound(id))) { app =>
          if(app.ownerId == req.user.id)
            action(app)(req)
          else
            Future successful Forbidden(views.html.apps.notfound(id))
        }
      )
    }

  def list = security.Authenticated.async { implicit req =>
    appCao.findForOwner(req.user.id).map { apps =>
      Ok(views.html.apps.list(apps))
    }
  }

  def create = security.Authenticated { implicit req =>
    Ok(views.html.apps.create(appForm))
  }

  def doCreate = security.Authenticated.async { implicit req =>
      appForm.bindFromRequest.fold(
        f => Future.successful(BadRequest(views.html.apps.create(f))),
        app => appCao.create(req.user, app).map { a =>
          Redirect(routes.Apps.app(a.id))
        }
      )
    }


  def app(id: String) =  WithApp(id) { app => implicit req =>
    Future successful Ok(views.html.apps.app(app))
  }

  def edit(id: String) = WithApp(id) { app => implicit req =>
    Future successful Ok(views.html.apps.edit(app, appForm.fill(AppForm(app))))
  }

  def doEdit(id: String) = WithApp(id) { app => implicit req =>
    appForm.bindFromRequest.fold(
      f => Future successful BadRequest(views.html.apps.edit(app, f)),
      form => {
        val app2 = app.copy(
          name = form.name,
          description = form.description,
          uri = form.uri,
          iconUri = form.iconUri,
          redirectUris = form.redirectUris,
          isWebApp = form.isWebApp,
          isNativeApp = form.isNativeApp
        )
        appCao.set(app.id, app2).map { _ =>
          Redirect(routes.Apps.app(app.id))
        }
      }
    )
  }

  def delete(id: String) = WithApp(id) { app => implicit req =>
    Future successful Ok(views.html.apps.delete(app))
  }

  def doDelete(id: String) = WithApp(id) { app => implicit req =>
    appCao.remove(id).map(_ =>
      Redirect(routes.Apps.list).flashing("success" -> Messages("flash.app.delete.success", app.name))
    )
  }
}
