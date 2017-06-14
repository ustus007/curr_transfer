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
import test.revolut.json.DeleteTransferRequest
import test.revolut.exceptions.NotEnoughParametersException
import java.util.stream.Collectors
import com.fasterxml.jackson.datatype.jsr310.JSR310Module

class DeleteTransfer extends HttpServlet {

  var transferManager: TransferManager = new TransferManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(new JSR310Module())

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("DeleteTransfer. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: DeleteTransferRequest = mapper.readValue(data, classOf[DeleteTransferRequest])
      if (parsed.uuid == null) {
        throw new NotEnoughParametersException
      }
      val result = transferManager.deleteTransfer(parsed.uuid)
      if (result) { "{\"success\":\"Transfer with id = " + parsed.uuid + " deleted\"}" } else { "{\"failure\":\"Deletion of transfer with id = " + parsed.uuid + " failed\"}" }
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("DeleteTransfer. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}