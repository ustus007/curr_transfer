package test.revolut.json

class WithdrawFromAccountRequest (var uuid:String, var takenSum: BigDecimal) {
  def this(){this(null, null)}
  
}