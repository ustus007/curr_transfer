package test.revolut.manager.impl

import test.revolut.manager.AccountManager
import test.revolut.entity.Account
import test.revolut.DAO.impl.AccountDAOImpl
import test.revolut.DAO.AccountDAO
import test.revolut.entity.User
import test.revolut.entity.Currency
import test.revolut.exceptions.IncorrectSumException
import test.revolut.exceptions.NotEnoughBallanceException
import test.revolut.DAO.impl.SynchronizationDAOImpl
import test.revolut.DAO.SynchronizationDAO
import test.revolut.DAO.impl.TransferDAOImpl
import test.revolut.DAO.TransferDAO
import test.revolut.DAO.DAOStorage

class AccountManagerImpl extends AccountManager {

  var accountDAO: AccountDAO = DAOStorage.getOrCreate("accountDAO", () => {new AccountDAOImpl}).asInstanceOf[AccountDAO]
  var syncDAO: SynchronizationDAO = new SynchronizationDAOImpl
  var transferDAO: TransferDAO = DAOStorage.getOrCreate("transferDAO", () => {new TransferDAOImpl}).asInstanceOf[TransferDAO]

  override def createAccount(currency: Currency, owner: User): Account = {
    val ids = List(currency.uuid, owner.uuid)
    val sync = syncDAO.getSyncObjectByUuids(ids)
    var result: Account = null
    syncDAO.syncOn(sync, () => {
      result = accountDAO.createAccount(currency, owner)
    })
    result
  }

  override def findAccount(uuid: Option[String], amounts: Option[(BigDecimal, BigDecimal)], currency: Option[Currency], owner: Option[User]): List[Account] = {
    accountDAO.readAccount(if (uuid.isEmpty) None else Some(List(uuid.get)),
      if (amounts.isEmpty) None else Some(List(amounts.get)),
      if (currency.isEmpty) None else Some(List(currency.get)),
      if (owner.isEmpty) None else Some(List(owner.get)))
  }

  override def topUpAccountFromOutside(uuid: String, additionalSum: BigDecimal): Option[Account] = {
    if (additionalSum < BigDecimal(0)) {
      throw new IncorrectSumException
    }
    val acs = findAccount(Some(uuid), None, None, None)
    if (acs.isEmpty) {
      None
    } else {
      val ids = List(acs.last.uuid, acs.last.user.uuid, acs.last.currency.uuid) :::
        transferDAO.readTransfer(None, Some(List(acs.last)), Some(List(acs.last)), None, None, None).map { x => x.uuid }
      val sync = syncDAO.getSyncObjectByUuids(ids)
      var result: Option[Account] = null
      syncDAO.syncOn(sync, () => {
        result = accountDAO.updateAccount(uuid, Some(acs.last.amount + additionalSum))
      })
      result
    }
  }

  override def withdrawFromAccountOutside(uuid: String, takenSum: BigDecimal): Option[Account] = {
    if (takenSum < BigDecimal(0)) {
      throw new IncorrectSumException
    }
    val acs = findAccount(Some(uuid), None, None, None)
    if (acs.isEmpty) {
      None
    } else {
      if (takenSum > acs.last.amount) {
        throw new NotEnoughBallanceException(acs.last.uuid)
      }

      val ids = List(acs.last.uuid, acs.last.user.uuid, acs.last.currency.uuid) :::
        transferDAO.readTransfer(None, Some(List(acs.last)), Some(List(acs.last)), None, None, None).map { x => x.uuid }
      val sync = syncDAO.getSyncObjectByUuids(ids)
      var result: Option[Account] = null
      syncDAO.syncOn(sync, () => {
        result = accountDAO.updateAccount(uuid, Some(acs.last.amount - takenSum))
      })
      result
    }
  }

  override def deleteAccount(uuid: String): Boolean = {
    val acs = findAccount(Some(uuid), None, None, None)
    if (acs.isEmpty) {
      false
    } else {
      val ids = List(acs.last.uuid, acs.last.user.uuid) :::
        transferDAO.readTransfer(None, Some(List(acs.last)), Some(List(acs.last)), None, None, None).map { x => x.uuid }
      val sync = syncDAO.getSyncObjectByUuids(ids)
      var result: Boolean = false
      syncDAO.syncOn(sync, () => {
        result = accountDAO.deleteAccount(uuid, true)
      })
      result
    }

  }

}