package test.revolut.servlets

import test.revolut.utilities.Util.Log
import java.util.stream.Collectors

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import test.revolut.exceptions.NotEnoughParametersException
import test.revolut.json.FindUserRequest
import test.revolut.manager.UserManager
import test.revolut.manager.impl.UserManagerImpl
import test.revolut.utilities.Util.resultOrExcept
import test.revolut.json.UserSearchResponse

class FindUser extends HttpServlet {

  var userManager: UserManager = new UserManagerImpl
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val data = req.getReader().lines().collect(Collectors.joining())
    Log.debug("FindUser. Data: "+data)
    val res = resultOrExcept(() => {
      val parsed: FindUserRequest = mapper.readValue(data, classOf[FindUserRequest])
      val uuid = if (parsed.uuid == null) {None} else {Some(parsed.uuid)}
      val fullName = if (parsed.fullName == null) {None} else {Some(parsed.fullName)}
      val result = userManager.findUser(uuid, fullName)
      mapper.writeValueAsString(result.map { x => new UserSearchResponse(x) })
    })
    resp.setStatus(res._1);
    resp.setContentType("application/json")
    Log.debug("FindUser. Response: "+res._2)
    resp.getWriter().println(res._2);
  }

}