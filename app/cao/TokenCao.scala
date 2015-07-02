package cao

import java.time.Instant
import javax.inject.{Inject, Singleton}
import com.sandinh.couchbase.access.JsCao1
import fr.njin.playoauth.common.domain.{OauthTokenRepository, OauthTokenFactory}
import models._
import pk.auth.CB
import pk.auth.CBType.TToken
import scala.concurrent.Future
import com.sandinh.util.TimeUtil._
import scala.concurrent.duration._
import com.sandinh.couchbase.Implicits._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TokenCao @Inject() (cb: CB) extends JsCao1[AuthToken, String](cb.authBucket)
    with OauthTokenFactory[AuthToken]
    with OauthTokenRepository[AuthToken]{
  import cb.{authBucket => bk}

  protected def key(id: String) = "pk.auth.t:" + id

  //expiry for refreshToken
  override protected def expiry(): Int = (Instant.now + 60.days).getEpochSecond.toInt

  def apply(ownerId: String, clientId: String, redirectUri: Option[String], scopes: Option[Seq[String]]): Future[AuthToken] = {
    val value = uuid()
    val code = AuthToken(value, ownerId, clientId, "Bearer", scopes = scopes, refreshToken = Some(uuid()))
    this.setT(value, code)
  }

  def find(value: String): Future[Option[AuthToken]] = this.get(value).optNotExist

  def revoke(value: String): Future[Option[AuthToken]] = this.get(value)
    .flatMap(t => this.setT(value, t.copy(revokeAt = Some(Instant.now))))
    .optNotExist

  private val N1QLRefreshToken =
    s"""SELECT t.* FROM ${bk.name} AS t USE INDEX (refresh_token USING GSI)
        |WHERE refreshToken = $$1 AND tpe = $TToken
        |LIMIT 1""".stripMargin

  def revokeByRefreshToken(value: String): Future[Option[AuthToken]] = this.query1(N1QLRefreshToken, value)
}
