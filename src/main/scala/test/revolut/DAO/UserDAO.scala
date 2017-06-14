package test.revolut.DAO

import test.revolut.data.storage.{ InMemory => st }
import test.revolut.{ entity => en }

trait UserDAO extends AbstractDAO {

  def transformUser(internal: st.User): en.User
  def transformUser(internal: st.User, accounts: List[en.Account]): en.User
  def createUser(name: String): en.User
  def readUser(uuids: Option[List[String]], fullNames: Option[List[String]]): List[en.User]
  def updateUser(uuid: String, fullName: Option[String], accounts: Option[List[en.Account]]): Option[en.User]
  def deleteUser(uuid: String): Boolean

}