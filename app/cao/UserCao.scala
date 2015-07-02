package cao

import javax.inject.{Inject, Singleton}
import com.couchbase.client.java.query.consistency.ScanConsistency
import com.couchbase.client.java.query.QueryParams
import com.sandinh.couchbase.access.JsCao1
import com.sandinh.couchbase.document.JsDocument
import fr.njin.playoauth.common.domain.OauthResourceOwnerRepository
import models._
import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt
import pk.auth.CB
import pk.auth.CBType.TUser
import com.couchbase.client.java.query.consistency.ScanConsistency._
import java.util.concurrent.TimeUnit.SECONDS
import org.joda.time.DateTimeZone.UTC
import scala.concurrent.Future
import com.sandinh.couchbase.Implicits._
import play.api.data.validation.Constraints.emailAddress
import play.api.data.validation._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserCao @Inject() (cb: CB) extends JsCao1[User, String](cb.authBucket) with OauthResourceOwnerRepository[User] {
  import cb.{authBucket => bk}

  protected def key(id: String) = "pk.auth.u:" + id

  private val N1QLByEmail =
    s"""SELECT a.* FROM ${bk.name} AS a USE INDEX (acc_by_email USING GSI)
       |WHERE email = $$1 AND tpe = $TUser
       |LIMIT 1""".stripMargin

  def byEmail(email: String, consistency: ScanConsistency = NOT_BOUNDED): Future[Option[User]] = {
    val qparams =
      if (consistency == NOT_BOUNDED) null
      else QueryParams.build().consistency(consistency).scanWait(1, SECONDS)
    this.query1(N1QLByEmail, qparams, email)
  }

  def find(id: String): Future[Option[User]] = this.get(id).optNotExist

  def byNameOrEmail(nameOrEmail: String): Future[Option[User]] =
    emailAddress(nameOrEmail) match {
      case Valid => byEmail(nameOrEmail)
      case _ => find(nameOrEmail)
    }

  def create(a: Signup): Future[User] = {
    val id = a.username
    val acc = User(id, a.email, BCrypt.hashpw(a.password, BCrypt.gensalt()), DateTime.now(UTC))
    bk.insert(JsDocument(key(id), acc))
      .map(_ => acc)
  }

  def checkPw(u: User, plaintext: String) = BCrypt.checkpw(plaintext, u.password)
}
