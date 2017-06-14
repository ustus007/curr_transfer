package test.revolut.manager.impl

import test.revolut.manager.TransferManager
import test.revolut.DAO.impl.SynchronizationDAOImpl
import test.revolut.DAO.impl.TransferDAOImpl
import test.revolut.DAO.TransferDAO
import test.revolut.DAO.SynchronizationDAO
import test.revolut.entity.Account
import test.revolut.DAO.impl.AccountDAOImpl
import test.revolut.DAO.AccountDAO
import test.revolut.exceptions.AccountNotFoundException
import test.revolut.exceptions.DifferentCurrenciesException
import test.revolut.exceptions.NotEnoughBallanceException
import test.revolut.entity.Transfer
import java.time.LocalDateTime
import test.revolut.exceptions.TransferAlreadyRolledBackException
import test.revolut.exceptions.TransferNotFoundException
import test.revolut.DAO.DAOStorage

class TransferManagerImpl extends TransferManager {

  var syncDAO: SynchronizationDAO = new SynchronizationDAOImpl
  var transferDAO: TransferDAO = DAOStorage.getOrCreate("transferDAO", () => {new TransferDAOImpl}).asInstanceOf[TransferDAO]
  var accountDAO: AccountDAO = DAOStorage.getOrCreate("accountDAO", () => {new AccountDAOImpl}).asInstanceOf[AccountDAO]

  override def makeTransfer(from: Account, to: Account, amount: BigDecimal): Transfer = {

    val froms = accountDAO.readAccount(Some(List(from.uuid)), None, None, None)
    val tos = accountDAO.readAccount(Some(List(to.uuid)), None, None, None)
    if (froms.size == 0) {
      throw new AccountNotFoundException(from.uuid)
    }
    if (tos.size == 0) {
      throw new AccountNotFoundException(to.uuid)
    }
    if (from.currency != to.currency) {
      throw new DifferentCurrenciesException
    }
    if (from.amount < amount) {
      throw new NotEnoughBallanceException(from.uuid)
    }
    val ids = List(from.uuid, to.uuid, from.user.uuid, to.user.uuid)
    val sync = syncDAO.getSyncObjectByUuids(ids)
    var result: Transfer = null

    syncDAO.syncOn(sync, () => {
      accountDAO.updateAccount(from.uuid, Some(from.amount - amount))
      accountDAO.updateAccount(to.uuid, Some(to.amount + amount))
      result = transferDAO.createTransfer(from, to, amount)

    })

    result

  }

  override def findTransfers(uuids: Option[List[String]],
    fromOptions: Option[List[Account]],
    toOptions: Option[List[Account]],
    amounts: Option[List[(BigDecimal, BigDecimal)]],
    times: Option[List[(LocalDateTime, LocalDateTime)]],
    rollbackStatuses: Option[List[Boolean]]): List[Transfer] = {
    transferDAO.readTransfer(uuids, fromOptions, toOptions, amounts, times, rollbackStatuses)
  }

  override def rollbackTransfer(uuid: String): Boolean = {
    val trsfrs = transferDAO.readTransfer(Some(List(uuid)), None, None, None, None, None)
    if (trsfrs.size == 0) {
      throw new TransferNotFoundException(uuid)
    }
    val tfr = trsfrs.last
    if (tfr.rolledBack) {
      throw new TransferAlreadyRolledBackException(uuid)
    }
    
    if (tfr.to.amount < tfr.amount) {
      throw new NotEnoughBallanceException(tfr.to.uuid)
    }
    
    val ids = List(tfr.from.uuid, tfr.to.uuid, tfr.from.user.uuid, tfr.to.user.uuid, uuid)
    val sync = syncDAO.getSyncObjectByUuids(ids)
    var result: Boolean = false

    syncDAO.syncOn(sync, () => {
      accountDAO.updateAccount(tfr.to.uuid, Some(tfr.to.amount - tfr.amount))
      accountDAO.updateAccount(tfr.from.uuid, Some(tfr.from.amount + tfr.amount))
      result = transferDAO.updateTransfer(uuid, Some(true))!=None
    })
    
    result
  }
  
  override def deleteTransfer(uuid: String): Boolean = {
    val trsfrs = transferDAO.readTransfer(Some(List(uuid)), None, None, None, None, None)
    if (trsfrs.size == 0) {
      throw new TransferNotFoundException(uuid)
    }
    val tfr = trsfrs.last
    val ids = List(tfr.from.uuid, tfr.to.uuid, tfr.from.user.uuid, tfr.to.user.uuid, uuid)
    val sync = syncDAO.getSyncObjectByUuids(ids)
    var result: Boolean = false
    syncDAO.syncOn(sync, () => {
       result=transferDAO.deleteTransfer(uuid)
    })
    result
  }

}