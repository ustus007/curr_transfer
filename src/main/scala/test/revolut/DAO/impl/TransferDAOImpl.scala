package test.revolut.DAO.impl

import test.revolut.data.storage.{ InMemory => st }
import test.revolut.{ entity => en }
import test.revolut.utilities.Util.generateUuid
import test.revolut.DAO.AccountDAO
import java.time.LocalDateTime
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import test.revolut.DAO.TransferDAO
import test.revolut.DAO.DAOStorage

class TransferDAOImpl extends TransferDAO {

  DAOStorage.named.synchronized({
    DAOStorage.named.put("transferDAO", this)
  })

  var accountDAO: AccountDAO = DAOStorage.getOrCreate("accountDAO", () => { new AccountDAOImpl }).asInstanceOf[AccountDAO]

  override def transformTransfer(internal: st.Transfer): en.Transfer = {
    val result = en.Transfer(internal.uuid,
      accountDAO.transformAccount(internal.from),
      accountDAO.transformAccount(internal.to),
      internal.amount,
      internal.moment,
      internal.rolledBack)
    return result
  }

  override def createTransfer(from: en.Account, to: en.Account, amount: BigDecimal): en.Transfer = {
    val representation = st.Transfer(generateUuid,
      st.storage.accounts.filter { x => x.uuid.equals(from.uuid) }.last,
      st.storage.accounts.filter { x => x.uuid.equals(to.uuid) }.last,
      amount,
      LocalDateTime.now,
      false)
    st.storage.transfers += representation
    return transformTransfer(representation)
  }

  override def readTransfer(uuids: Option[List[String]],
    fromOptions: Option[List[en.Account]],
    toOptions: Option[List[en.Account]],
    amounts: Option[List[(BigDecimal, BigDecimal)]],
    times: Option[List[(LocalDateTime, LocalDateTime)]],
    rollbackStatuses: Option[List[Boolean]]): List[en.Transfer] = {
    st.storage.transfers.filter { x =>
      {
        if (uuids == None &&
          fromOptions == None &&
          toOptions == None &&
          amounts == None &&
          times == None &&
          rollbackStatuses == None) {
          true
        } else {
          var result: Boolean = false
          if (uuids != None) {
            result = uuids.get.contains(x.uuid)
          }
          if (!result && fromOptions != None) {
            result = fromOptions.get.map { x => x.uuid }.contains(x.from.uuid)
          }
          if (!result && toOptions != None) {
            result = toOptions.get.map { x => x.uuid }.contains(x.to.uuid)
          }
          if (!result && amounts != None) {
            result = amounts.get.foldLeft(false)((value: Boolean, limits: (BigDecimal, BigDecimal)) => {
              val min = limits._1
              val max = limits._2
              value || (min <= x.amount && max >= x.amount)
            })
          }
          if (!result && times != None) {
            result = times.get.foldLeft(false)((value: Boolean, period: (LocalDateTime, LocalDateTime)) => {
              val start = period._1
              val end = period._2
              value || (start.isBefore(x.moment) && end.isAfter(x.moment) || start.equals(x.moment) || end.equals(x.moment))
            })
          }
          if (!result && rollbackStatuses != None) {
            result = rollbackStatuses.get.contains(x.rolledBack)
          }
          result
        }
      }
    }.map { x => transformTransfer(x) }.toList
  }

  override def updateTransfer(uuid: String, rollbackStatus: Option[Boolean]): Option[en.Transfer] = {
    val sr: ArrayBuffer[st.Transfer] = st.storage.transfers.filter { x => x.uuid.equals(uuid) }
    if (sr.size == 0) {
      None
    } else {
      val newRollbackStatus = if (rollbackStatus == None) { sr.last.rolledBack } else { rollbackStatus.get }
      sr.last.rolledBack = newRollbackStatus
      Some(transformTransfer(sr.last))
    }
  }

  override def deleteTransfer(uuid: String): Boolean = {
    val sr: ArrayBuffer[st.Transfer] = st.storage.transfers.filter { x => x.uuid.equals(uuid) }
    if (sr.size == 0) {
      false
    } else {
      val trsfr = sr.last
      st.storage.transfers -= trsfr
      true
    }
  }

}