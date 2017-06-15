package test.revolut.manager.impl

import test.revolut.manager.UserManager
import test.revolut.entity.User
import test.revolut.DAO.UserDAO
import test.revolut.DAO.impl.UserDAOImpl
import test.revolut.DAO.impl.SynchronizationDAOImpl
import test.revolut.DAO.SynchronizationDAO
import test.revolut.DAO.impl.TransferDAOImpl
import test.revolut.DAO.TransferDAO
import test.revolut.DAO.DAOStorage

class UserManagerImpl extends UserManager {

  var userDAO: UserDAO = DAOStorage.getOrCreate("userDAO", () => { new UserDAOImpl }).asInstanceOf[UserDAO]
  var syncDAO: SynchronizationDAO = new SynchronizationDAOImpl
  var transferDAO: TransferDAO = DAOStorage.getOrCreate("transferDAO", () => {new TransferDAOImpl}).asInstanceOf[TransferDAO]

  override def createUser(name: String): User = {
    userDAO.createUser(name)
  }

  override def findUser(uuid: Option[String], name: Option[String]): List[User] = {
    userDAO.readUser(if (uuid.isEmpty) None else Some(List(uuid.get)),
      if (name.isEmpty) None else Some(List(name.get)))
  }

  override def renameUser(uuid: String, newName: String): Option[User] = {
    val usrs = findUser(Some(uuid), None)
    if (usrs.size == 0) {
      None
    } else {
      var result: Option[User] = null
      val ids = List(usrs.last.uuid)
      val sync = syncDAO.getSyncObjectByUuids(ids)
      syncDAO.syncOn(sync, () => {
        result = userDAO.updateUser(uuid, Some(newName))
      })
      result
    }
  }

  override def deleteUser(uuid: String): Boolean = {
    val usrs = findUser(Some(uuid), None)
    if (usrs.size == 0) {
      false
    } else {
      val usr = usrs.last
      val acs = usr.accounts
      val trsfrs = transferDAO.readTransfer(None, Some(acs.toList), Some(acs.toList), None, None, None)
      val ids = usr.uuid :: acs.map { x => x.uuid }.toList ::: trsfrs.map { x => x.uuid }
      val sync = syncDAO.getSyncObjectByUuids(ids)
      var result: Boolean = false
      syncDAO.syncOn(sync, () => {
        result = userDAO.deleteUser(uuid)
      })
      result
    }
  }

}