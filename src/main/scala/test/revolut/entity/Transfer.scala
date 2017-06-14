package test.revolut.entity

import java.time.LocalDateTime

case class Transfer(uuid:String, from: Account, to: Account, amount: BigDecimal, moment: LocalDateTime, var rolledBack: Boolean) extends Entity {
  
}