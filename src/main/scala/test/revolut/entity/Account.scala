package test.revolut.entity

case class Account(uuid:String, currency: Currency, amount: BigDecimal,var user: User) extends Entity{
  
}