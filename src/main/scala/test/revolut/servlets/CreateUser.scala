package test.revolut.servlets

import test.revolut.utilities.Util.Log
import test.revolut.utilities.Util.resultOrExcept
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.http.HttpStatus
import test.revolut.manager.impl.UserManagerImpl
import com.fasterxml.jackson.databind.ObjectMapper
import test.revolut.manager.UserManager
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import test.revolut.json.CreateUserRequest
import test.revolut.exceptions.NotEnoughParametersException
import java.util.stream.Collectors
import test.revolut.json.UserSearchResponse

class CreateUser extends HttpServlet {

  var userManager: UserManager = new UserManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("CreateUser. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: CreateUserRequest = mapper.readValue(data, classOf[CreateUserRequest])
      if (parsed.fullName == null) {
        throw new NotEnoughParametersException
      }
      val result = userManager.createUser(parsed.fullName)
      mapper.writeValueAsString(new UserSearchResponse(result))
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("CreateUser. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}