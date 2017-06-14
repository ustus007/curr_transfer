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
import test.revolut.json.RenameUserRequest
import test.revolut.exceptions.NotEnoughParametersException
import java.util.stream.Collectors
import test.revolut.exceptions.UserNotFoundException
import test.revolut.json.UserSearchResponse

class RenameUser extends HttpServlet {

  var userManager: UserManager = new UserManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("RenameUser. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: RenameUserRequest = mapper.readValue(data, classOf[RenameUserRequest])
      if (parsed.uuid == null || parsed.fullName == null) {
        throw new NotEnoughParametersException
      }
      val result = userManager.renameUser(parsed.uuid, parsed.fullName)
      if (result == None) {
        throw new UserNotFoundException(parsed.uuid)
      }
      mapper.writeValueAsString(new UserSearchResponse(result.get))
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("RenameUser. Response: "+res._2)
    resp.getWriter().println(res._2);
  }

}