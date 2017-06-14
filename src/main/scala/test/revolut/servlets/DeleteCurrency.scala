package test.revolut.servlets

import test.revolut.utilities.Util.Log
import test.revolut.utilities.Util.resultOrExcept
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.http.HttpStatus
import test.revolut.manager.CurrencyManager
import com.fasterxml.jackson.databind.ObjectMapper
import test.revolut.manager.impl.CurrencyManagerImpl
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import test.revolut.exceptions.NotEnoughParametersException
import test.revolut.json.DeleteCurrencyRequest
import java.util.stream.Collectors

class DeleteCurrency extends HttpServlet {

  var currencyManager: CurrencyManager = new CurrencyManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("DeleteCurrency. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: DeleteCurrencyRequest = mapper.readValue(data, classOf[DeleteCurrencyRequest])
      if (parsed.uuid == null) {
        throw new NotEnoughParametersException
      }
      val uuid = parsed.uuid
      val result = currencyManager.deleteCurrency(uuid)
      if (result) { "{\"success\":\"Currency with id = " + uuid + " deleted\"}" } else { "{\"failure\":\"Deletion of currency with id = " + uuid + " failed\"}" }
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("DeleteCurrency. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}