package test.revolut.json

import java.time.LocalDateTime
import test.revolut.entity.Transfer

class TransferSearchResponse(var uuid:String,var from: AccountSearchResponse,var  to: AccountSearchResponse,var amount: BigDecimal,var moment: LocalDateTime, var rolledBack: Boolean) {
  def this (transfer:Transfer){this(transfer.uuid, 
      new AccountSearchResponse(transfer.from), 
      new AccountSearchResponse(transfer.to), 
      transfer.amount,
      transfer.moment,
      transfer.rolledBack)}
  
}