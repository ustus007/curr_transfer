package test.revolut.servlets

import test.revolut.utilities.Util.Log
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.http.HttpStatus
import test.revolut.utilities.Util.resultOrExcept
import test.revolut.manager.impl.AccountManagerImpl
import com.fasterxml.jackson.databind.ObjectMapper
import test.revolut.manager.AccountManager
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.util.stream.Collectors
import test.revolut.manager.UserManager
import test.revolut.json.CreateAccountRequest
import test.revolut.manager.impl.CurrencyManagerImpl
import test.revolut.exceptions.NotEnoughParametersException
import test.revolut.manager.impl.UserManagerImpl
import test.revolut.json.UserSearchResponse
import test.revolut.manager.CurrencyManager
import test.revolut.json.AccountSearchResponse
import test.revolut.exceptions.CurrencyNotFoundException
import test.revolut.exceptions.UserNotFoundException

class CreateAccount extends HttpServlet {

  var userManager: UserManager = new UserManagerImpl
  var currencyManager: CurrencyManager = new CurrencyManagerImpl
  var accountManager: AccountManager = new AccountManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("CreateAccount. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: CreateAccountRequest = mapper.readValue(data, classOf[CreateAccountRequest])
      if (parsed.currencyUuid == null || parsed.ownerUuid == null) {
        throw new NotEnoughParametersException
      }
      val currency = currencyManager.findCurrency(Some(parsed.currencyUuid), None, None)
      if (currency.size == 0) {
        throw new CurrencyNotFoundException(parsed.currencyUuid)
      }
      val owner = userManager.findUser(Some(parsed.ownerUuid), None)
      if (owner.size == 0) {
        throw new UserNotFoundException(parsed.ownerUuid)
      }
      val result = accountManager.createAccount(currency.last, owner.last)
      mapper.writeValueAsString(new AccountSearchResponse(result))
    })
    
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("CreateAccount. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}