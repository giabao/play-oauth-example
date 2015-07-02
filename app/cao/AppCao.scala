package cao

import java.time.Instant
import javax.inject.{Inject, Singleton}
import com.sandinh.couchbase.access.JsCao1
import controllers.Apps.AppForm
import fr.njin.playoauth.common.domain.OauthClientRepository
import models._
import pk.auth.CB
import com.sandinh.couchbase.Implicits._
import scala.concurrent.Future

@Singleton
class AppCao @Inject() (cb: CB) extends JsCao1[App, String](cb.authBucket)
    with OauthClientRepository[App] {
  protected def key(id: String) = "pk.auth.a:" + id

  def find(id: String): Future[Option[App]] = this.get(id).optNotExist

  def findForOwner(ownerId: String): Future[List[App]] = Future successful Nil //FIXME

  def create(owner: User, d: AppForm): Future[App] = {
    val id = uuid()
    val secret = uuid()
    val app = App(id, owner.id, secret, d.name, d.description, d.uri, d.iconUri, d.redirectUris,
      d.isWebApp, d.isNativeApp, Instant.now)
    setT(id, app)
  }
}
