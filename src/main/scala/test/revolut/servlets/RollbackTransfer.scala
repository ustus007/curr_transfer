package test.revolut.servlets

import org.eclipse.jetty.http.HttpStatus
import test.revolut.utilities.Util.Log
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import com.fasterxml.jackson.databind.ObjectMapper
import test.revolut.manager.TransferManager
import test.revolut.manager.impl.TransferManagerImpl
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.util.stream.Collectors
import test.revolut.json.TransferSearchResponse
import test.revolut.json.MakeTransferRequest
import test.revolut.exceptions.NotEnoughParametersException
import test.revolut.exceptions.AccountNotFoundException
import test.revolut.json.RollbackTransferRequest
import test.revolut.utilities.Util.resultOrExcept
import com.fasterxml.jackson.datatype.jsr310.JSR310Module

class RollbackTransfer extends HttpServlet {

  var transferManager: TransferManager = new TransferManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(new JSR310Module())
  
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("RollbackTransfer. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: RollbackTransferRequest = mapper.readValue(data, classOf[RollbackTransferRequest])
      if (parsed.uuid == null) {
        throw new NotEnoughParametersException
      }
      val result = transferManager.rollbackTransfer(parsed.uuid)
      if (result) { "{\"success\":\"Transfer with id = " + parsed.uuid + " rolled back\"}" } else { "{\"failure\":\"Rolling back transfer with id = " + parsed.uuid + " failed\"}" }
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("RollbackTransfer. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}