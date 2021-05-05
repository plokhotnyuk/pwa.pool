package pool

import java.time.Instant

sealed trait Event extends Product with Serializable

final case class HealthChecked(datetime: String = Instant.now.toString) extends Event

final case class SignedUp(licensee: Licensee) extends Event

final case class LicenseeActivated(licensee: Licensee) extends Event

final case class SignedIn(licensee: Licensee) extends Event

final case class LicenseeDeactivated(licensee: Licensee) extends Event