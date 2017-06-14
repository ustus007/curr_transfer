package test.revolut.servlets

import test.revolut.utilities.Util.Log
import test.revolut.utilities.Util.resultOrExcept
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.http.HttpStatus
import com.fasterxml.jackson.databind.ObjectMapper
import test.revolut.manager.TransferManager
import test.revolut.manager.impl.TransferManagerImpl
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import test.revolut.json.MakeTransferRequest
import test.revolut.exceptions.NotEnoughParametersException
import java.util.stream.Collectors
import test.revolut.json.UserSearchResponse
import test.revolut.manager.impl.AccountManagerImpl
import test.revolut.manager.AccountManager
import test.revolut.exceptions.AccountNotFoundException
import test.revolut.json.TransferSearchResponse
import com.fasterxml.jackson.datatype.jsr310.JSR310Module

class MakeTransfer extends HttpServlet {

  var accountManager: AccountManager = new AccountManagerImpl
  var transferManager: TransferManager = new TransferManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(new JSR310Module())

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("MakeTransfer. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: MakeTransferRequest = mapper.readValue(data, classOf[MakeTransferRequest])
      if (parsed.amount == null || parsed.fromUuid == null || parsed.toUuid == null ) {
        throw new NotEnoughParametersException
      }
      val from = accountManager.findAccount(Some(parsed.fromUuid), None, None, None)
      if (from.size == 0){
        throw new AccountNotFoundException(parsed.fromUuid)
      }
      val to = accountManager.findAccount(Some(parsed.toUuid), None, None, None)
      if (to.size == 0){
        throw new AccountNotFoundException(parsed.toUuid)
      }
      val result = transferManager.makeTransfer(from.last, to.last, parsed.amount)
      mapper.writeValueAsString(new TransferSearchResponse(result))
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("MakeTransfer. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}