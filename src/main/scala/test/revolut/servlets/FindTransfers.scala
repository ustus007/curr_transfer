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
import test.revolut.json.FindTransfersRequest
import com.fasterxml.jackson.datatype.jsr310.JSR310Module
import test.revolut.manager.impl.AccountManagerImpl
import test.revolut.manager.AccountManager
import test.revolut.entity.Account
import test.revolut.exceptions.AccountNotFoundException
import java.time.LocalDateTime
import test.revolut.json.TransferSearchResponse

class FindTransfers extends HttpServlet {

  var accountManager: AccountManager = new AccountManagerImpl
  var transferManager: TransferManager = new TransferManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(new JSR310Module())

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("FindTransfers. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: FindTransfersRequest = mapper.readValue(data, classOf[FindTransfersRequest])

      var uuids: Option[List[String]] = None
      if (parsed.uuids != null) {
        uuids = Some(parsed.uuids)
      }
      var fromOptions: Option[List[Account]] = None
      if (parsed.fromUuids != null) {
        fromOptions = Some(parsed.fromUuids.map { x =>
          {
            val ac = accountManager.findAccount(Some(x), None, None, None)
            if (ac.size == 0) {
              throw new AccountNotFoundException(x)
            }
            ac.last
          }
        })
      }
      var toOptions: Option[List[Account]] = None
      if (parsed.toUuids != null) {
        toOptions = Some(parsed.toUuids.map { x =>
          {
            val ac = accountManager.findAccount(Some(x), None, None, None)
            if (ac.size == 0) {
              throw new AccountNotFoundException(x)
            }
            ac.last
          }
        })
      }
      var amounts: Option[List[(BigDecimal, BigDecimal)]] = None
      if (parsed.amounts != null) {
        amounts = Some(parsed.amounts)
      }
      var times: Option[List[(LocalDateTime, LocalDateTime)]] = None
      if (parsed.times != null) {
        times = Some(parsed.times)
      }
      var rollbackStatuses: Option[List[Boolean]] = None
      if (parsed.rollbackStatuses != null) {
        rollbackStatuses = Some(parsed.rollbackStatuses)
      }

      val result = transferManager.findTransfers(uuids, fromOptions, toOptions, amounts, times, rollbackStatuses)
       mapper.writeValueAsString(result.map(x => new TransferSearchResponse(x)))
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("FindTransfers. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}