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
import test.revolut.json.TopUpAccountRequest
import java.util.stream.Collectors
import test.revolut.exceptions.CurrencyNotFoundException
import test.revolut.exceptions.NotEnoughParametersException
import test.revolut.exceptions.UserNotFoundException
import test.revolut.json.AccountSearchResponse
import test.revolut.exceptions.AccountNotFoundException

class TopUpAccount extends HttpServlet {

  var accountManager: AccountManager = new AccountManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("TopUpAccount. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: TopUpAccountRequest = mapper.readValue(data, classOf[TopUpAccountRequest])
      if (parsed.uuid == null || parsed.additionalSum == null) {
        throw new NotEnoughParametersException
      }
      val result = accountManager.topUpAccountFromOutside(parsed.uuid, parsed.additionalSum)
      if (result == None){
        throw new AccountNotFoundException(parsed.uuid)
      }
      mapper.writeValueAsString(new AccountSearchResponse(result.get))
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("TopUpAccount. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}