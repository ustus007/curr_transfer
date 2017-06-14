package test.revolut.manager

import test.revolut.entity.User

trait UserManager extends AbstractManager {
  
  def createUser(name: String): User
  def findUser(uuid: Option[String], name: Option[String]): List[User]
  def renameUser(uuid: String, newName: String): Option[User]
   def deleteUser(uuid: String): Boolean
  
}