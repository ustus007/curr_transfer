package test.revolut.entity

import java.time.LocalDateTime

case class Transfer(uuid:String, from: Account, to: Account, amount: BigDecimal, moment: LocalDateTime, var rolledBack: Boolean) extends Entity {
  override def hashCode:Int = {
    uuid.hashCode()
  }
  def equals(that: Transfer): Boolean = this.uuid.equals(that.uuid)
  def ==(that: Transfer) = this equals that
}