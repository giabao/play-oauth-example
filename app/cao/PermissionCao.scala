package cao

import javax.inject.{Inject, Singleton}
import com.sandinh.couchbase.access.JsCao2
import fr.njin.playoauth.common.domain.OauthResourceOwnerPermission
import models.Permission
import pk.auth.CB
import com.sandinh.couchbase.Implicits._
import scala.concurrent.Future

/**
 * Our permission provider for the authorization endpoint
 *
 * This provider search the last created and non revoked permission for
 * the user and the client. If the found permission is not granted and
 * is not the lastPermission, the provider filters the permission
 * in order to allow the endpoint to ask another one to the user
 *
 * param lastPermission: Option[Long]
 */
@Singleton
class PermissionCao @Inject() (cb: CB) extends JsCao2[Permission, String, String](cb.authBucket)
    with OauthResourceOwnerPermission[Permission] {

  protected def key(ownerId: String, clientId: String) = s"pk.auth.p:$ownerId,$clientId"

  //FIXME expiry?

  def apply(ownerId: String, clientId: String): Future[Option[Permission]] = this.get(ownerId, clientId).optNotExist
}
