package tripletail

object Validators {
  implicit class StringOps(val value: String) {
    import java.time.{LocalDate, LocalTime}
    import java.time.format.DateTimeFormatter
    import scala.util.Try

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    def <(length: Int): Boolean = if (value.nonEmpty) value.length < length else false
    def <=(length: Int): Boolean = if (value.nonEmpty) value.length <= length else false
    def ===(length: Int): Boolean = if (value.nonEmpty) value.length == length else false
    def >(length: Int): Boolean = if (value.nonEmpty) value.length > length else false
    def >=(length: Int): Boolean = if (value.nonEmpty) value.length >= length else false
    def isDate: Boolean = Try(LocalDate.parse(value, dateFormatter)).isSuccess
    def isTime: Boolean = Try(LocalTime.parse(value, timeFormatter)).isSuccess
  }

  object SignupValidator {
    implicit class Ops(val signup: Signup) {
      def isValid: Boolean = signup.email.nonEmpty
    }
  }

  object SigninValidator {
    implicit class Ops(val signin: Signin) {
      def isValid: Boolean = signin.email.nonEmpty
    }
  }

  object LicenseeValidator {
    implicit class Ops(val licensee: Licensee) {
      def isValid: Boolean = {
        licensee.license.length == 36 &&
        licensee.email.nonEmpty &&
        licensee.activated >= 0
      }
    }
  }

  object PoolValidator {
    implicit class Ops(val pool: Pool) {
      def isValid: Boolean = {
        pool.id >= 0 &&
        pool.license.nonEmpty &&
        pool.built > 0 &&
        pool.lat >= 0 &&
        pool.lon >= 0 &&
        pool.volume > 1000
      }
    }
  }

  object PoolIdValidator {
    implicit class Ops(val poolId: PoolId) {
      def isValid: Boolean = poolId.id > 0
    }
  }

  object SurfaceValidator {
    implicit class Ops(val surface: Surface) {
      def isValid(surface: Surface): Boolean = {
        surface.id >= 0 &&
        surface.poolId > 0 &&
        surface.installed > 0 &&
        surface.kind.nonEmpty
      }
    }
  }

  object PumpValidator {
    implicit class Ops(val pump: Pump) {
      def isValid: Boolean = {
        pump.id >= 0 &&
        pump.poolId > 0 &&
        pump.installed > 0 &&
        pump.model.nonEmpty
      }
    }
  }

  object TimerValidator {
    implicit class Ops(val timer: Timer) {
      def isValid: Boolean = {
        timer.id >= 0 &&
        timer.poolId > 0 &&
        timer.installed > 0 &&
        timer.model.nonEmpty
      }
    }
  }

  object TimerIdValidator {
    implicit class Ops(val timerId: TimerId) {
      def isValid: Boolean = timerId.id > 0
    }
  }

  object TimerSettingValidator {
    implicit class Ops(val timerSetting: TimerSetting) {
      def isValid: Boolean = {
        timerSetting.id >= 0 &&
        timerSetting.timerId > 0 &&
        timerSetting.set > 0 &&
        timerSetting.setOn > 0 &&
        timerSetting.setOff > 0
      }
    }
  }

  object HeaterValidator {
    implicit class Ops(val heater: Heater) {
      def isValid: Boolean = {
        heater.id >= 0 &&
        heater.poolId > 0 &&
        heater.installed > 0 &&
        heater.model.nonEmpty
      }
    }
  }

  object HeaterIdValidator {
    implicit class Ops(val heaterId: HeaterId) {
      def isValid: Boolean = heaterId.id > 0
    }
  }

  object HeaterOnValidator {
    implicit class Ops(val heaterOn: HeaterOn) {
      def isValid: Boolean = {
        heaterOn.id >= 0 &&
        heaterOn.heaterId > 0 &&
        heaterOn.temp > 0 &&
        heaterOn.set > 0
      }
    }
  }

  object HeaterOffValidator {
    implicit class Ops(val heaterOff: HeaterOff) {
      def isValid: Boolean = {
        heaterOff.id >= 0 &&
        heaterOff.heaterId > 0 &&
        heaterOff.set > 0
      }
    }
  }

  object CleaningValidator {
    implicit class Ops(val cleaning: Cleaning) {
      def isValid: Boolean = {
        cleaning.id >= 0 &&
        cleaning.poolId > 0 &&
        cleaning.cleaned > 0 &&
        cleaning.brush &&
        cleaning.net &&
        cleaning.vacuum &&
        cleaning.skimmerBasket &&
        cleaning.pumpBasket &&
        cleaning.pumpFilter &&
        cleaning.pumpChlorineTablets >= 0 &&
        cleaning.deck
      }
    }
  }

  object MeasurementValidator {
    private val temp = 32 to 95
    private val totalHardness = 1 to 1000
    private val totalChlorine = 0 to 10
    private val totalBromine = 0 to 20
    private val freeChlorine = 0 to 10
    private val totalAlkalinity = 0 to 240
    private val cyanuricAcid = 0 to 300
    implicit class Ops(val measurement: Measurement) {
      def isValid: Boolean = {
        measurement.id >= 0 &&
        measurement.poolId > 0 &&
        measurement.measured > 0 &&
        temp.contains(measurement.temp) &&
        totalHardness.contains(measurement.totalHardness) &&
        totalChlorine.contains(measurement.totalChlorine) &&
        totalBromine.contains(measurement.totalBromine) &&
        freeChlorine.contains(measurement.freeChlorine) &&
        (measurement.ph >= 6.2 && measurement.ph <= 8.4) &&
        totalAlkalinity.contains(measurement.totalAlkalinity) &&
        cyanuricAcid.contains(measurement.cyanuricAcid)
      }
    }
  }

  object ChemicalValidator {
    implicit class Ops(val chemical: Chemical) {
      def isValid: Boolean = {
        chemical.id >= 0 &&
        chemical.poolId > 0 &&
        chemical. added > 0 &&
        chemical.chemical.nonEmpty &&
        chemical.amount > 0.00 &&
        chemical.unit.nonEmpty
      }
    }
  }

  object SupplyValidator {
    implicit class Ops(val supply: Supply) {
      def isValid: Boolean = {
        supply.id >= 0 &&
        supply.poolId > 0 &&
        supply.purchased > 0 &&
        supply.cost > 0.00 &&
        supply.item.nonEmpty &&
        supply.amount > 0.00 &&
        supply.unit.nonEmpty
      }
    }
  }

  object RepairValidator {
    implicit class Ops(val repair: Repair) {
      def isValid: Boolean = {
        repair.id >= 0 &&
        repair.poolId > 0 &&
        repair.repaired > 0 &&
        repair.cost > 0.00 &&
        repair.repair.nonEmpty
      }
    }
  }
}