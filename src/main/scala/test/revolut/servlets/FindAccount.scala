package test.revolut.servlets

import test.revolut.utilities.Util.Log
import test.revolut.utilities.Util.resultOrExcept
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.http.HttpStatus
import test.revolut.manager.impl.AccountManagerImpl
import com.fasterxml.jackson.databind.ObjectMapper
import test.revolut.manager.AccountManager
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.util.stream.Collectors
import test.revolut.exceptions.CurrencyNotFoundException
import test.revolut.exceptions.NotEnoughParametersException
import test.revolut.exceptions.UserNotFoundException
import test.revolut.json.AccountSearchResponse
import test.revolut.manager.impl.UserManagerImpl
import test.revolut.manager.CurrencyManager
import test.revolut.manager.impl.CurrencyManagerImpl
import test.revolut.manager.UserManager
import test.revolut.json.FindAccountRequest
import test.revolut.entity.Currency
import test.revolut.entity.User

class FindAccount extends HttpServlet {

  var userManager: UserManager = new UserManagerImpl
  var currencyManager: CurrencyManager = new CurrencyManagerImpl
  var accountManager: AccountManager = new AccountManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  override def doPost(req: HttpServletRequest,
    resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("FindAccount. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed : FindAccountRequest = mapper.readValue(data, classOf[FindAccountRequest])
      var currency:Option[Currency] = None
      if (parsed.currencyUuid != null) {
        val currency_ = currencyManager.findCurrency(Some(parsed.currencyUuid), None, None)
        if (currency_.size == 0) {
          throw new CurrencyNotFoundException(parsed.currencyUuid)
        }
        currency = Some(currency_.last)
      }
      var owner:Option[User] = None
      if (parsed.ownerUuid != null) {
        val owner_ = userManager.findUser(Some(parsed.ownerUuid), None)
        if (owner_.size == 0) {
          throw new UserNotFoundException(parsed.ownerUuid)
        }
        owner = Some(owner_.last)
      }
      var uuid:Option[String] = None
      if (parsed.uuid != null) {
        uuid = Some(parsed.uuid)
      }
      var amounts:Option[(BigDecimal, BigDecimal)] = None
      if (parsed.amounts!= null){
        amounts= Some(parsed.amounts)
      }
      val result = accountManager.findAccount(uuid, amounts, currency, owner)
      mapper.writeValueAsString(result.map(x => new AccountSearchResponse(x)))
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("FindAccount. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}