import play.api.libs.json._
import play.api.libs.json.Reads.LongReads
import scala.concurrent.duration._

package object models {
  implicit val DurationReads: Reads[FiniteDuration] = LongReads.map(new FiniteDuration(_, MILLISECONDS))
  implicit object DurationWrites extends Writes[FiniteDuration] {
    def writes(o: FiniteDuration) = JsNumber(o.toMillis)
  }
}
