package test.revolut.json

class TopUpAccountRequest (var uuid:String, var additionalSum: BigDecimal) {
  def this(){this(null, null)}
  
}