package test.revolut.DAO

import test.revolut.data.storage.{ InMemory => st }
import test.revolut.{ entity => en }

trait AccountDAO extends AbstractDAO {

  def transformAccount(internal: st.Account): en.Account
  def transformAccount(internal: st.Account, user: en.User): en.Account
  def createAccount(currency: en.Currency, owner: en.User): en.Account
  def readAccount(uuids: Option[List[String]], amounts: Option[List[(BigDecimal, BigDecimal)]], currencies: Option[List[en.Currency]], owners:Option[List[en.User]]):List[en.Account]
  def updateAccount(uuid: String, amount: Option[BigDecimal]): Option[en.Account]
  def deleteAccount(uuid: String, fromUsers: Boolean): Boolean

}