package cao

import javax.inject.{Inject, Singleton}
import com.sandinh.couchbase.access.JsCao1
import fr.njin.playoauth.common.OAuth
import fr.njin.playoauth.common.domain.{OauthCodeRepository, OauthCodeFactory}
import models._
import pk.auth.CB
import scala.concurrent.Future
import com.sandinh.couchbase.Implicits._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CodeCao @Inject() (cb: CB) extends JsCao1[AuthCode, String](cb.authBucket)
    with OauthCodeFactory[AuthCode]
    with OauthCodeRepository[AuthCode] {
  protected def key(id: String) = "pk.auth.c:" + id

  //expiry for refreshToken
  override protected def expiry(): Int = OAuth.MaximumLifetime.toSeconds.toInt

  def apply(ownerId: String, clientId: String, redirectUri: Option[String], scopes: Option[Seq[String]]): Future[AuthCode] = {
    val value = uuid()
    val code = AuthCode(value, ownerId, clientId, scopes = scopes, redirectUri = redirectUri)
    this.setT(value, code)
  }

  def find(value: String): Future[Option[AuthCode]] = this.get(value).optNotExist

  def revoke(value: String): Future[Option[AuthCode]] = this.remove(value).map(_.as[AuthCode]).optNotExist
}
