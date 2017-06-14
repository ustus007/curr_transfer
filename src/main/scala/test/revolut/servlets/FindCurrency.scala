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
import test.revolut.json.FindCurrencyRequest

class FindCurrency extends HttpServlet {

  var currencyManager: CurrencyManager = new CurrencyManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("FindCurrency. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: FindCurrencyRequest = mapper.readValue(data, classOf[FindCurrencyRequest])
      val uuid = if (parsed.uuid==null) {None} else {Some(parsed.uuid)}
      val shortName = if (parsed.shortName==null) {None} else {Some(parsed.shortName)}
      val fullName = if (parsed.fullName==null) {None} else {Some(parsed.fullName)}
      val result = currencyManager.findCurrency(uuid, shortName, fullName)
      mapper.writeValueAsString(result)
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("FindCurrency. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}