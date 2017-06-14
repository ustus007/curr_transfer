package test.revolut.manager

import java.time.LocalDateTime
import test.revolut.entity.Account
import test.revolut.entity.Transfer

trait TransferManager extends AbstractManager {
  
  def makeTransfer(from: Account, to: Account, amount: BigDecimal): Transfer
  def findTransfers(uuids: Option[List[String]],
    fromOptions: Option[List[Account]],
    toOptions: Option[List[Account]],
    amounts: Option[List[(BigDecimal, BigDecimal)]],
    times: Option[List[(LocalDateTime, LocalDateTime)]],
    rollbackStatuses: Option[List[Boolean]]): List[Transfer]
  def rollbackTransfer(uuid: String): Boolean
  def deleteTransfer(uuid: String): Boolean
  
}