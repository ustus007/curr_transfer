package test.revolut.DAO.impl

import test.revolut.DAO.UserDAO
import test.revolut.data.storage.{ InMemory => st }
import test.revolut.{ entity => en }
import test.revolut.utilities.Util.generateUuid
import scala.collection.mutable.HashSet
import test.revolut.DAO.CurrencyDAO
import test.revolut.DAO.AccountDAO
import test.revolut.DAO.DAOStorage

class UserDAOImpl extends UserDAO {

  DAOStorage.named.synchronized({
    DAOStorage.named.put("userDAO", this)
  })

  var currencyDAO: CurrencyDAO = DAOStorage.getOrCreate("currencyDAO", () => { new CurrencyDAOImpl }).asInstanceOf[CurrencyDAO]
  var accountDAO: AccountDAO = DAOStorage.getOrCreate("accountDAO", () => { new AccountDAOImpl }).asInstanceOf[AccountDAO]

  override def transformUser(internal: st.User): en.User = {
    val result = en.User(internal.uuid, internal.fullName, Set.empty[en.Account])
    val accounts = internal.accounts.map { x => accountDAO.transformAccount(x, result) }.toSet
    result.accounts = accounts
    return result
  }

  override def transformUser(internal: st.User, accounts: List[en.Account]): en.User = {
    val result = en.User(internal.uuid, internal.fullName, accounts.toSet)
    return result
  }

  override def createUser(name: String): en.User = {
    val representation = st.User(generateUuid, name, new st.PSet[st.Account])
    st.storage.users += representation
    return transformUser(representation)
  }

  override def readUser(uuids: Option[List[String]], fullNames: Option[List[String]]): List[en.User] = {
    st.storage.users.filter { x =>
      {
        if (uuids == None && fullNames == None) {
          true
        } else {
          var result: Boolean = false
          if (uuids != None) {
            result = uuids.get.contains(x.uuid)
          }
          if (!result && fullNames != None) {
            result = fullNames.get.contains(x.fullName)
          }
          result
        }
      }
    }.map { x => transformUser(x) }.toList
  }

  override def updateUser(uuid: String, fullName: Option[String], accounts: Option[List[en.Account]]): Option[en.User] = {
    val sr: HashSet[st.User] = st.storage.users.filter { x => x.uuid.equals(uuid) }
    if (sr.size == 0) {
      None
    } else {
      val newFullName = if (fullName == None) { sr.last.fullName } else { fullName.get }
      val newAccounts = if (accounts == None) { sr.last.accounts } else {
        accounts.get.map { x =>
          {
            st.Account(x.uuid, st.storage.currencies.filter { y => y.uuid.equals(x.currency.uuid) }.last, x.amount, sr.last)
          }
        }
      }
      sr.last.fullName = newFullName
      sr.last.accounts.synchronized({
        sr.last.accounts.clear
        sr.last.accounts ++= newAccounts
      })

      Some(transformUser(sr.last))
    }
  }

  override def deleteUser(uuid: String): Boolean = {
    val sr: HashSet[st.User] = st.storage.users.filter { x => x.uuid.equals(uuid) }
    if (sr.size == 0) {
      false
    } else {
      val usr = sr.last
      st.storage.users.remove(usr)
      usr.accounts.foreach { x => accountDAO.deleteAccount(x.uuid, false) }
      true
    }
  }

}