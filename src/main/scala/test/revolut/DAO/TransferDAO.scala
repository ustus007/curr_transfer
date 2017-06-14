package test.revolut.DAO

import test.revolut.data.storage.{ InMemory => st }
import test.revolut.{ entity => en }
import java.time.LocalDateTime

trait TransferDAO  extends AbstractDAO {
  def transformTransfer(internal: st.Transfer): en.Transfer
  def createTransfer(from: en.Account, to: en.Account, amount: BigDecimal): en.Transfer
  def readTransfer(uuids: Option[List[String]], 
      fromOptions: Option[List[en.Account]], 
      toOptions: Option[List[en.Account]], 
      amounts: Option[List[(BigDecimal, BigDecimal)]],
      times: Option[List[(LocalDateTime, LocalDateTime)]],
      rollbackStatuses: Option[List[Boolean]]): List[en.Transfer]
  def updateTransfer(uuid: String, rollbackStatus: Option[Boolean]): Option[en.Transfer]
  def deleteTransfer(uuid: String): Boolean
}