package test.revolut.servlets

import test.revolut.utilities.Util.Log
import test.revolut.utilities.Util.resultOrExcept
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.http.HttpStatus
import java.util.stream.Collectors
import test.revolut.manager.CurrencyManager
import test.revolut.manager.impl.CurrencyManagerImpl
import com.fasterxml.jackson.databind.ObjectMapper
import test.revolut.json.CreateCurrencyRequest
import com.fasterxml.jackson.databind.DeserializationFeature
import test.revolut.exceptions.NotEnoughParametersException
import com.fasterxml.jackson.module.scala.DefaultScalaModule

class CreateCurrency extends HttpServlet {

  var currencyManager: CurrencyManager = new CurrencyManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {

    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("CreateCurrency. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: CreateCurrencyRequest = mapper.readValue(data, classOf[CreateCurrencyRequest])
      if (parsed.fullName == null || parsed.shortName == null) {
        throw new NotEnoughParametersException
      }
      val result = currencyManager.addCurrency(parsed.shortName, parsed.fullName)
      mapper.writeValueAsString(result)
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("CreateCurrency. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}