package test.revolut.application

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler

import test.revolut.servlets.CreateAccount
import test.revolut.servlets.CreateCurrency
import test.revolut.servlets.CreateUser
import test.revolut.servlets.DeleteAccount
import test.revolut.servlets.DeleteCurrency
import test.revolut.servlets.DeleteTransfer
import test.revolut.servlets.DeleteUser
import test.revolut.servlets.FindAccount
import test.revolut.servlets.FindCurrency
import test.revolut.servlets.FindTransfers
import test.revolut.servlets.FindUser
import test.revolut.servlets.MakeTransfer
import test.revolut.servlets.RenameCurrency
import test.revolut.servlets.RenameUser
import test.revolut.servlets.RollbackTransfer
import test.revolut.servlets.TopUpAccount
import test.revolut.servlets.WithdrawFromAccount

object ServerHolder {

  var server: Server = null
  System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog")
  System.setProperty("org.eclipse.jetty.LEVEL", "OFF")

  def initServer(port: Int) {
    server = new Server(port)

    val servlets = Map("/currency/create" -> classOf[CreateCurrency],
      "/currency/delete" -> classOf[DeleteCurrency],
      "/currency/find" -> classOf[FindCurrency],
      "/currency/rename" -> classOf[RenameCurrency],
      "/user/create" -> classOf[CreateUser],
      "/user/delete" -> classOf[DeleteUser],
      "/user/find" -> classOf[FindUser],
      "/user/rename" -> classOf[RenameUser],
      "/account/create" -> classOf[CreateAccount],
      "/account/delete" -> classOf[DeleteAccount],
      "/account/find" -> classOf[FindAccount],
      "/account/top" -> classOf[TopUpAccount],
      "/account/withdraw" -> classOf[WithdrawFromAccount],
      "/transfer/make" -> classOf[MakeTransfer],
      "/transfer/delete" -> classOf[DeleteTransfer],
      "/transfer/rollback" -> classOf[RollbackTransfer],
      "/transfer/find" -> classOf[FindTransfers])

    val handler: ServletHandler = new ServletHandler()
    server.setHandler(handler)

    for (s: String <- servlets.keys) {
      handler.addServletWithMapping(servlets(s), s);
    }

    server.start

  }

  def stopServer() {
    if (server != null) {
      server.stop
    }
  }

}