package test.revolut.entity

case class Account(uuid:String, currency: Currency, amount: BigDecimal,var user: User) extends Entity{
  override def hashCode:Int = {
    uuid.hashCode()
  }
  def equals(that: Account): Boolean = this.uuid.equals(that.uuid)
  def ==(that: Account) = this equals that
}