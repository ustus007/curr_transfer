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
import java.util.stream.Collectors
import test.revolut.json.RenameCurrencyRequest
import test.revolut.exceptions.CurrencyNotFoundException

class RenameCurrency extends HttpServlet {

  var currencyManager: CurrencyManager = new CurrencyManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("RenameCurrency. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: RenameCurrencyRequest = mapper.readValue(data, classOf[RenameCurrencyRequest])
      if (parsed.uuid == null || (parsed.fullName == null && parsed.shortName == null)) {
        throw new NotEnoughParametersException
      }
      val uuid = parsed.uuid
      val shortName = if (parsed.shortName == null) { None } else { Some(parsed.shortName) }
      val fullName = if (parsed.fullName == null) { None } else { Some(parsed.fullName) }
      val result = currencyManager.renameCurrency(uuid, shortName, fullName)
      if (result == None) {
        throw new CurrencyNotFoundException(uuid)
      }
      mapper.writeValueAsString(result.get)
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("RenameCurrency. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}