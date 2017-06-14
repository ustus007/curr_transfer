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
import test.revolut.json.DeleteAccountRequest
import test.revolut.exceptions.NotEnoughParametersException
import test.revolut.json.AccountSearchResponse
import test.revolut.exceptions.AccountNotFoundException

class DeleteAccount extends HttpServlet {

  var accountManager: AccountManager = new AccountManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("DeleteAccount. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: DeleteAccountRequest = mapper.readValue(data, classOf[DeleteAccountRequest])
      if (parsed.uuid == null) {
        throw new NotEnoughParametersException
      }
      val result = accountManager.deleteAccount(parsed.uuid)
      if (result) { "{\"success\":\"Account with id = " + parsed.uuid + " deleted\"}" } else { "{\"failure\":\"Deletion of account with id = " + parsed.uuid + " failed\"}" }
    })
    resp.setStatus(res._1)
    resp.setContentType("application/json")
    Log.debug("DeleteAccount. Response: "+res._2)
    resp.getWriter().println(res._2)

  }

}