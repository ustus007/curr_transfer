package test.revolut.utilities

import com.typesafe.scalalogging._
import org.eclipse.jetty.http.HttpStatus
import org.json4s._
import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s.jackson.JsonMethods._
import test.revolut.exceptions.AccountNotFoundException
import test.revolut.exceptions.DifferentCurrenciesException
import test.revolut.exceptions.IncorrectSumException
import test.revolut.exceptions.NotEnoughBallanceException
import test.revolut.exceptions.TransferAlreadyRolledBackException
import test.revolut.exceptions.TransferNotFoundException
import test.revolut.exceptions.NotEnoughParametersException
import test.revolut.exceptions.CurrencyNotFoundException
import test.revolut.exceptions.UserNotFoundException
import org.slf4j.LoggerFactory

object Util {

  def generateUuid: String = {
    return java.util.UUID.randomUUID.toString
  }

  private def renderError(error: (String, String)): (Int, String) = {
    (HttpStatus.INTERNAL_SERVER_ERROR_500, compact(render(error)))
  }

  def resultOrExcept(action: () => String): (Int, String) = {

    try {
      (HttpStatus.OK_200, action())
    } catch {
      case e: AccountNotFoundException => { Log.error(e.getMessage, e); renderError(("error" -> ("Could not find account with id = " + e.uuid))) }
      case e: CurrencyNotFoundException => { Log.error(e.getMessage, e); renderError(("error" -> ("Could not find currency with id = " + e.uuid))) }
      case e: UserNotFoundException => { Log.error(e.getMessage, e); renderError(("error" -> ("Could not find user with id = " + e.uuid))) }
      case e: DifferentCurrenciesException => { Log.error(e.getMessage, e); renderError(("error" -> "The currencies for provided accounts should match")) }
      case e: IncorrectSumException => { Log.error(e.getMessage, e); renderError(("error" -> "Provided amount is incorrect")) }
      case e: NotEnoughParametersException => { Log.error(e.getMessage, e); renderError(("error" -> "Provided JSON has not enough parameters for this request")) }
      case e: NotEnoughBallanceException => { Log.error(e.getMessage, e); renderError(("error" -> ("Amount is not enough for account with id = " + e.uuid))) }
      case e: TransferAlreadyRolledBackException => { Log.error(e.getMessage, e); renderError(("error" -> ("Transfer already rolled back: id = " + e.uuid))) }
      case e: TransferNotFoundException => { Log.error(e.getMessage, e); renderError(("error" -> ("Could not find transfer with id = " + e.uuid))) }
      case e: Exception => { Log.error(e.getMessage, e); renderError(("error" -> "Unknown error")) }
    }

  }

  def Log: Logger = { Logger(LoggerFactory.getLogger(this.getClass))}

}