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
import test.revolut.exceptions.NotEnoughParametersException
import java.util.stream.Collectors
import test.revolut.json.UserSearchResponse
import test.revolut.json.DeleteUserRequest

class DeleteUser extends HttpServlet {
  
  var userManager: UserManager = new UserManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("DeleteUser. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: DeleteUserRequest = mapper.readValue(data, classOf[DeleteUserRequest])
      if (parsed.uuid == null) {
        throw new NotEnoughParametersException
      }
      val result = userManager.deleteUser(parsed.uuid)
      if (result) { "{\"success\":\"User with id = " + parsed.uuid + " deleted\"}" } else { "{\"failure\":\"Deletion of user with id = " + parsed.uuid + " failed\"}" }
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("DeleteUser. Response: "+res._2)
    resp.getWriter().println(res._2);

  }

}