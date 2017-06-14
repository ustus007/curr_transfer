package test.revolut.DAO.impl

import test.revolut.DAO.CurrencyDAO
import test.revolut.data.storage.{ InMemory => st }
import test.revolut.{ entity => en }
import test.revolut.utilities.Util.generateUuid
import scala.collection.mutable.HashSet
import test.revolut.DAO.AccountDAO
import test.revolut.DAO.DAOStorage

class CurrencyDAOImpl extends CurrencyDAO {

  DAOStorage.named.synchronized({
    DAOStorage.named.put("currencyDAO", this)
  })
  
  var accountDAO: AccountDAO = DAOStorage.getOrCreate("accountDAO", () => {new AccountDAOImpl}).asInstanceOf[AccountDAO]

  override def transformCurrency(internal: st.Currency): en.Currency = {
    val result = en.Currency(internal.uuid, internal.shortName, internal.fullName)
    return result
  }

  override def createCurrency(shortName: String, fullName: String): en.Currency = {
    val representation = st.Currency(generateUuid, shortName, fullName)
    st.storage.currencies += representation
    return transformCurrency(representation)
  }

  override def readCurrency(uuids: Option[List[String]], shortNames: Option[List[String]], fullNames: Option[List[String]]): List[en.Currency] = {
    st.storage.currencies.filter { x =>
      {
        if (uuids == None && shortNames == None && fullNames == None) {
          true
        } else {
          var result: Boolean = false
          if (uuids != None) {
            result = uuids.get.contains(x.uuid)
          }
          if (!result && shortNames != None) {
            result = shortNames.get.contains(x.shortName)
          }
          if (!result && fullNames != None) {
            result = fullNames.get.contains(x.fullName)
          }
          result
        }
      }
    }.map { x => transformCurrency(x) }.toList
  }

  override def updateCurrency(uuid: String, shortName: Option[String], fullName: Option[String]): Option[en.Currency] = {
    val sr: HashSet[st.Currency] = st.storage.currencies.filter { x => x.uuid.equals(uuid) }
    if (sr.size == 0) {
      None
    } else {
      val newShortName = if (shortName == None) { sr.last.shortName } else { shortName.get }
      val newFullName = if (fullName == None) { sr.last.fullName } else { fullName.get }
      sr.last.shortName = newShortName
      sr.last.fullName = newFullName
      Some(transformCurrency(sr.last))
    }
  }

  override def deleteCurrency(uuid: String): Boolean = {
    val sr: HashSet[st.Currency] = st.storage.currencies.filter { x => x.uuid.equals(uuid) }
    if (sr.size == 0) {
      false
    } else {
      val crc = sr.last
      st.storage.accounts.filter { x => x.currency.uuid.equals(crc.uuid) }.foreach { x => accountDAO.deleteAccount(x.uuid, true) }
      st.storage.currencies.remove(crc)
      true
    }
  }

}