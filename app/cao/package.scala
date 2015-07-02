import java.util.UUID

package object cao {
  @inline def uuid() = UUID.randomUUID().toString
}
