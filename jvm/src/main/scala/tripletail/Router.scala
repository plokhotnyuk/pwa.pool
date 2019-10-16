package tripletail

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import de.heikoseeberger.akkahttpupickle.UpickleSupport._
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

object Router {
  def apply(store: Store, licenseeCache: LicenseeCache, emailer: ActorRef): Router = new Router(store, licenseeCache, emailer)
}

class Router(store: Store, licenseeCache: LicenseeCache, emailer: ActorRef) {
  import Serializers._
  import Validators._
  import StatusCodes._
  import licenseeCache._
  import store._

  val logger = LoggerFactory.getLogger(Router.getClass)

  val onUnauthorized = (cause: String) => {
    logger.error(cause)
    addFault(Fault(cause, Unauthorized.intValue))
  }

  val onInvalid = (entity: Entity) => {
    val cause = s"*** Invalid: $entity"
    logger.error(cause)
    addFault(Fault(cause, BadRequest.intValue))
  }

  implicit val onException = ExceptionHandler {
    case NonFatal(error) =>
      extractRequestContext { context =>
        val cause = s"*** Handling ${context.request.uri} failed: ${error.getMessage}"
        logger.error(cause)
        context.request.discardEntityBytes(context.materializer)
        complete(InternalServerError -> addFault(Fault(cause)))
      }
  }

  val index = path("") {
    getFromResource("index.html")
  }
  val resources = get {
    getFromResourceDirectory("./")
  }
  val signup = path("signup") {
    post {
      entity(as[SignUp]) { signup =>
        if (signup.isValid) {
          onSuccess(signUp(signup.email)) { licensee =>
            extractRequestContext { context =>
              emailer ! SendEmail(to = signup.email, license = licensee.license, uri = context.request.uri.toString)
              complete(OK -> SignedUp(licensee))
            }
          }
        } else complete(BadRequest -> onInvalid(signup))
      }
    }
  }
  val activatelicense = path("activatelicense" / Segment) { license: String =>
    get {
      onSuccess(activateLicense(license, DateTime.currentDate)) { activatedDate =>
        complete(OK -> activatedDate)
      }
    }
  }
  val signin = path("signin") {
    post {
      entity(as[SignIn]) { signin =>
        if (signin.isValid) {
          onSuccess(signIn(signin.license, signin.email)) {
            case Some(licensee) =>
              cacheLicensee(licensee)
              complete(OK -> SignedIn(licensee))
            case None =>
              val cause = s"*** Unauthorized license: ${signin.license} and/or email: ${signin.email}"
              complete(Unauthorized -> onUnauthorized(cause))
          }
        } else complete(BadRequest -> onInvalid(signin))
      }
    }
  }
  val pools = path("pools") {
    post {
      entity(as[Licensee]) { licensee =>
        if (licensee.isValid) onSuccess(listPools(licensee.license)) { pools => complete(OK -> Pools(pools)) }
        else {
          val cause = s"*** Unauthorized licensee: $licensee"
          complete(Unauthorized -> onUnauthorized(cause))
        }
      }
    }
  } ~ pathSuffix("add") {
    post {
      entity(as[Pool]) { pool =>
        if (pool.isValid) onSuccess(addPool(pool)) { id => complete(OK -> Id(id)) }
        else complete(BadRequest -> onInvalid(pool))
      }
    }
  } ~ pathSuffix("update") {
    post {
      entity(as[Pool]) { pool =>
        if (pool.isValid) onSuccess(updatePool(pool)) { count => complete(OK -> Count(count)) }
        else complete(BadRequest -> onInvalid(pool))
      }
    }
  }
  val surfaces = path("surfaces") {
    post {
      entity(as[PoolId]) { poolId =>
        if (poolId.isValid) onSuccess(listSurfaces(poolId.id)) { surfaces => complete(OK -> Surfaces(surfaces)) }
        else complete(BadRequest -> onInvalid(poolId))
      }
    }
  } ~ pathSuffix("add") {
    post {
      entity(as[Surface]) { surface =>
        if (surface.isValid) onSuccess(addSurface(surface)) { id => complete(OK -> Id(id)) }
        else complete(BadRequest -> onInvalid(surface))
      }
    }
  } ~ pathSuffix("update") {
    post {
      entity(as[Surface]) { surface =>
        if (surface.isValid) onSuccess(updateSurface(surface)) { count => complete(OK -> Count(count)) }
        else complete(BadRequest -> onInvalid(surface))
      }
    }
  }
  val pumps = path("pumps") {
    post {
      entity(as[PoolId]) { poolId =>
        if (poolId.isValid) onSuccess(listPumps(poolId.id)) { pumps => complete(OK -> Pumps(pumps)) }
        else complete(BadRequest -> onInvalid(poolId))
      }
    }
  } ~ pathSuffix("add") {
    post {
      entity(as[Pump]) { pump =>
        if (pump.isValid) onSuccess(addPump(pump)) { id => complete(OK -> Id(id)) }
        else complete(BadRequest -> onInvalid(pump))
      }
    }
  } ~ pathSuffix("update") {
    post {
      entity(as[Pump]) { pump =>
        if (pump.isValid) onSuccess(updatePump(pump)) { count => complete(OK -> Count(count)) }
        else complete(BadRequest -> onInvalid(pump))
      }
    }
  }
  val timers = path("timers") {
    post {
      entity(as[PoolId]) { poolId =>
        if (poolId.isValid) onSuccess(listTimers(poolId.id)) { timers => complete(OK -> Timers(timers)) }
        else complete(BadRequest -> onInvalid(poolId))
      }
    }
  } ~ pathSuffix("add") {
    post {
      entity(as[Timer]) { timer =>
        if (timer.isValid) onSuccess(addTimer(timer)) { id => complete(OK -> Id(id)) }
        else complete(BadRequest -> onInvalid(timer))
      }
    }
  } ~ pathSuffix("update") {
    post {
      entity(as[Timer]) { timer =>
        if (timer.isValid) onSuccess(updateTimer(timer)) { count => complete(OK -> Count(count)) }
        else complete(BadRequest -> onInvalid(timer))
      }
    }
  }
  val timersettings = path("timersettings") {
    post {
      entity(as[TimerId]) { timerId =>
        if (timerId.isValid) onSuccess(listTimerSettings(timerId.id)) { timersettings => complete(OK -> TimerSettings(timersettings)) }
        else complete(BadRequest -> onInvalid(timerId))
      }
    }
  } ~ pathSuffix("add") {
    post {
      entity(as[TimerSetting]) { timerSetting =>
        if (timerSetting.isValid) onSuccess(addTimerSetting(timerSetting)) { id => complete(OK -> Id(id)) }
        else complete(BadRequest -> onInvalid(timerSetting))
      }
    }
  } ~ pathSuffix("update") {
    post {
      entity(as[TimerSetting]) { timerSetting =>
        if (timerSetting.isValid) onSuccess(updateTimerSetting(timerSetting)) { count => complete(OK -> Count(count)) }
        else complete(BadRequest -> onInvalid(timerSetting))
      }
    }
  }
  val heaters = path("heaters") {
    post {
      entity(as[PoolId]) { poolId =>
        if (poolId.isValid) onSuccess(listHeaters(poolId.id)) { heaters => complete(OK -> Heaters(heaters)) }
        else complete(BadRequest -> onInvalid(poolId))
      }
    }
  } ~ pathSuffix("add") {
    post {
      entity(as[Heater]) { heater =>
        if (heater.isValid) onSuccess(addHeater(heater)) { id => complete(OK -> Id(id)) }
        else complete(BadRequest -> onInvalid(heater))
      }
    }
  } ~ pathSuffix("update") {
    post {
      entity(as[Heater]) { heater =>
        if (heater.isValid) onSuccess(updateHeater(heater)) { count => complete(OK -> Count(count)) }
        else complete(BadRequest -> onInvalid(heater))
      }
    }
  }
  val heatersettings = path("heatersettings") {
    post {
      entity(as[HeaterId]) { heaterId =>
        if (heaterId.isValid) onSuccess(listHeaterSettings(heaterId.id)) { heaterSettings => complete(OK -> HeaterSettings(heaterSettings)) }
        else complete(BadRequest -> onInvalid(heaterId))
      }
    }
  } ~ pathSuffix("add") {
    post {
      entity(as[HeaterSetting]) { heaterSetting =>
        if (heaterSetting.isValid) onSuccess(addHeaterSetting(heaterSetting)) { id => complete(OK -> Id(id)) }
        else complete(BadRequest -> onInvalid(heaterSetting))
      }
    }
  } ~ pathSuffix("update") {
    post {
      entity(as[HeaterSetting]) { heaterSetting =>
        if (heaterSetting.isValid) onSuccess(updateHeaterSetting(heaterSetting)) { count => complete(OK -> Count(count)) }
        else complete(BadRequest -> onInvalid(heaterSetting))
      }
    }
  }
  val cleanings = path("cleanings") {
    post {
      entity(as[PoolId]) { poolId =>
        if (poolId.isValid) onSuccess(listCleanings(poolId.id)) { cleanings => complete(OK -> Cleanings(cleanings)) }
        else complete(BadRequest -> onInvalid(poolId))
      }
    }
  } ~ pathSuffix("add") {
    post {
      entity(as[Cleaning]) { cleaning =>
        if (cleaning.isValid) onSuccess(addCleaning(cleaning)) { id => complete(OK -> Id(id)) }
        else complete(BadRequest -> onInvalid(cleaning))
      }
    }
  } ~ pathSuffix("update") {
    post {
      entity(as[Cleaning]) { cleaning =>
        if (cleaning.isValid) onSuccess(updateCleaning(cleaning)) { count => complete(OK -> Count(count)) }
        else complete(BadRequest -> onInvalid(cleaning))
      }
    }
  }
  val measurements = path("measurements") {
    post {
      entity(as[PoolId]) { poolId =>
        if (poolId.isValid) onSuccess(listMeasurements(poolId.id)) { measurements => complete(OK -> Measurements(measurements)) }
        else complete(BadRequest -> onInvalid(poolId))
      }
    }
  } ~ pathSuffix("add") {
    post {
      entity(as[Measurement]) { measurement =>
        if (measurement.isValid) onSuccess(addMeasurement(measurement)) { id => complete(OK -> Id(id)) }
        else complete(BadRequest -> onInvalid(measurement))
      }
    }
  } ~ pathSuffix("update") {
    post {
      entity(as[Measurement]) { measurement =>
        if (measurement.isValid) onSuccess(updateMeasurement(measurement)) { count => complete(OK -> Count(count)) }
        else complete(BadRequest -> onInvalid(measurement))
      }
    }
  }
  val chemicals = path("chemicals") {
    post {
      entity(as[PoolId]) { poolId =>
        if (poolId.isValid) onSuccess(listChemicals(poolId.id)) { chemicals => complete(OK -> Chemicals(chemicals)) }
        else complete(BadRequest -> onInvalid(poolId))
      }
    }
  } ~ pathSuffix("add") {
    post {
      entity(as[Chemical]) { chemical =>
        if (chemical.isValid) onSuccess(addChemical(chemical)) { id => complete(OK -> Id(id)) }
        else complete(BadRequest -> onInvalid(chemical))
      }
    }
  } ~ pathSuffix("update") {
    post {
      entity(as[Chemical]) { chemical =>
        if (chemical.isValid) onSuccess(updateChemical(chemical)) { count => complete(OK -> Count(count)) }
        else complete(BadRequest -> onInvalid(chemical))
      }
    }
  }
  val supplies = path("supplies") {
    post {
      entity(as[PoolId]) { poolId =>
        if (poolId.isValid) onSuccess(listSupplies(poolId.id)) { supplies => complete(OK -> Supplies(supplies)) }
        else complete(BadRequest -> onInvalid(poolId))
      }
    }
  } ~ pathSuffix("add") {
    post {
      entity(as[Supply]) { supply =>
        if (supply.isValid) onSuccess(addSupply(supply)) { id => complete(OK -> Id(id)) }
        else complete(BadRequest -> onInvalid(supply))
      }
    }
  } ~ pathSuffix("update") {
    post {
      entity(as[Supply]) { supply =>
        if (supply.isValid) onSuccess(updateSupply(supply)) { count => complete(OK -> Count(count)) }
        else complete(BadRequest -> onInvalid(supply))
      }
    }
  }
  val repairs = path("repairs") {
    post {
      entity(as[PoolId]) { poolId =>
        if (poolId.isValid) onSuccess(listRepairs(poolId.id)) { repairs => complete(OK -> Repairs(repairs)) }
        else complete(BadRequest -> onInvalid(poolId))
      }
    }
  } ~ pathSuffix("add") {
    post {
      entity(as[Repair]) { repair =>
        if (repair.isValid) onSuccess(addRepair(repair)) { id => complete(OK -> Id(id)) }
        else complete(BadRequest -> onInvalid(repair))
      }
    }
  } ~ pathSuffix("update") {
    post {
      entity(as[Repair]) { repair =>
        if (repair.isValid) onSuccess(updateRepair(repair)) { count => complete(OK -> Count(count)) }
        else complete(BadRequest -> onInvalid(repair))
      }
    }
  }
  val url = "/api/v1/tripletail"
  val api = pathPrefix("api" / "v1" / "tripletail") {
    signin ~ pools ~ surfaces ~ pumps ~ timers ~ timersettings ~ heaters ~ heatersettings ~
      cleanings ~ measurements ~ chemicals ~ supplies ~ repairs
  }
  val secure = (route: Route) => headerValueByName(Licensee.licenseHeaderKey) { license =>
    onSuccess(isLicenseActivated(license)) { isActivated =>
      if (isActivated) route
      else {
        val cause = s"*** License is not activated: $license"
        complete(Unauthorized -> onUnauthorized(cause))
      }
    }
  }
  val secureApi = secure { api }
  val routes = Route.seal( index ~ resources ~ signup ~ activatelicense ~ secureApi )
}