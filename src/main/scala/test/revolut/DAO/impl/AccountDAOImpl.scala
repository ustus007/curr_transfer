package test.revolut.DAO.impl

import test.revolut.DAO.AccountDAO
import test.revolut.data.storage.{ InMemory => st }
import test.revolut.{ entity => en }
import test.revolut.utilities.Util.generateUuid
import scala.collection.mutable.HashSet
import test.revolut.DAO.CurrencyDAO
import test.revolut.DAO.UserDAO
import test.revolut.DAO.TransferDAO
import test.revolut.DAO.DAOStorage

class AccountDAOImpl extends AccountDAO {

  DAOStorage.named.synchronized({
    DAOStorage.named.put("accountDAO", this)
  })

  var currencyDAO: CurrencyDAO = DAOStorage.getOrCreate("currencyDAO", () => { new CurrencyDAOImpl }).asInstanceOf[CurrencyDAO]
  var userDAO: UserDAO = DAOStorage.getOrCreate("userDAO", () => { new UserDAOImpl }).asInstanceOf[UserDAO]
  var transferDAO: TransferDAO = DAOStorage.getOrCreate("transferDAO", () => { new TransferDAOImpl }).asInstanceOf[TransferDAO]

  override def transformAccount(internal: st.Account): en.Account = {
    val result: en.Account = en.Account(internal.uuid,
      currencyDAO.transformCurrency(internal.currency.asInstanceOf[st.Currency]),
      internal.amount,
      null)
    val usr = userDAO.transformUser(internal.user, List(result))
    result.user = usr
    return result
  }

  override def transformAccount(internal: st.Account, user: en.User): en.Account = {
    val result = en.Account(internal.uuid, currencyDAO.transformCurrency(internal.currency), internal.amount, user)
    return result
  }

  override def createAccount(currency: en.Currency, owner: en.User): en.Account = {
    val usr = st.storage.users.filter { x => x.uuid.equals(owner.uuid) }.last
    val representation = st.Account(generateUuid,
      st.storage.currencies.filter { x => x.uuid.equals(currency.uuid) }.last,
      BigDecimal(0),
      usr)
    usr.accounts += representation
    st.storage.accounts += representation
    return transformAccount(representation)
  }

  override def readAccount(uuids: Option[List[String]], amounts: Option[List[(BigDecimal, BigDecimal)]], currencies: Option[List[en.Currency]], owners: Option[List[en.User]]): List[en.Account] = {
    st.storage.accounts.filter { x =>
      {
        if (uuids == None && amounts == None && currencies == None && owners == None) {
          true
        } else {
          var result: Boolean = false
          if (uuids != None) {
            result = uuids.get.contains(x.uuid)
          }
          if (!result && amounts != None) {
            result = amounts.get.foldLeft(false)((value: Boolean, limits: (BigDecimal, BigDecimal)) => {
              val min = limits._1
              val max = limits._2
              value || (min <= x.amount && max >= x.amount)
            })
          }
          if (!result && currencies != None) {
            result = currencies.get.map { y => y.uuid }.contains(x.currency.uuid)
          }
          if (!result && owners != None) {
            result = owners.get.map { y => y.uuid }.contains(x.user.uuid)
          }
          result
        }
      }
    }.map { x => transformAccount(x) }.toList
  }

  override def updateAccount(uuid: String, amount: Option[BigDecimal]): Option[en.Account] = {
    val sr: HashSet[st.Account] = st.storage.accounts.filter { x => x.uuid.equals(uuid) }
    if (sr.size == 0) {
      None
    } else {
      val newAmount = if (amount == None) { sr.last.amount } else { amount.get }
      sr.last.amount = newAmount
      Some(transformAccount(sr.last))
    }
  }

  override def deleteAccount(uuid: String, fromUsers: Boolean): Boolean = {
    val sr: HashSet[st.Account] = st.storage.accounts.filter { x => x.uuid.equals(uuid) }
    if (sr.size == 0) {
      false
    } else {
      val ac = sr.last
      transferDAO.readTransfer(None, Some(List(transformAccount(ac))), Some(List(transformAccount(ac))), None, None, None)
        .foreach { x => transferDAO.deleteTransfer(x.uuid) }
      st.storage.accounts.remove(ac)
      if (fromUsers) {
        st.storage.users.foreach { x => x.accounts.remove(ac) }
      }
      true
    }

  }

}
