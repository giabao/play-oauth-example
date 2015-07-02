import org.specs2.mutable._
import pk.auth.{Signin, SignupAcc}
import play.api.http.Status.{OK, BAD_REQUEST, CONFLICT, UNAUTHORIZED}
import play.api.libs.json.{Writes, Json}
import play.api.test.Helpers._
import play.api.test._

class AccountCtrlSpec extends Specification { sequential
  def test[A: Writes](o: A, code: Int = BAD_REQUEST, action: String = "signup") = {
    val req = FakeRequest(POST, s"/account/$action")
      .withJsonBody(implicitly[Writes[A]].writes(o))
    val signup = route(req).get
    status(signup) must equalTo(code)
  }

  implicit val wrt1 = Json.writes[SignupAcc]
  implicit val wrt2 = Json.writes[Signin]

  "AccountCtrl" should {
    """signup - note: you must manually run the following N1QL:
      |DELETE FROM pk USE INDEX (acc_by_email USING GSI) WHERE tpe=1 AND email is NOT MISSING;
      |UPSERT INTO pk(KEY, VALUE) VALUES ("pk.auth.ac.id", 0) RETURNING *;
    """.stripMargin in new WithApplication {
      test(Json.obj("foo" -> "baz"))
      test(SignupAcc("abc", "a12345", None))
      test(SignupAcc("abc@def.com", "a1234", None))
      test(SignupAcc("abc@def.com", "abcdef", None))
      test(SignupAcc("abc@def.com", "123456", None))
      test(SignupAcc("abc@def.com", "12345#", None))
      test(SignupAcc("abc@def.com", "a12345678901234567890", None))

      test(SignupAcc("abc@def.com", "a23456", None), OK)

      test(SignupAcc("abc@def.com", "abcde#", None), CONFLICT)

      test(SignupAcc("abc2@def.com", "a23456", Some(Json.obj("foo" -> "baz"))), OK)
    }

    "signin" in new WithApplication {
      test(Signin("abc2@def.com", "a123"), BAD_REQUEST, "signin")
      test(Signin("abc2@def.com", "a12345"), UNAUTHORIZED, "signin")
      test(Signin("abc2@def.com", "a23456"), OK, "signin")
    }
  }
}
