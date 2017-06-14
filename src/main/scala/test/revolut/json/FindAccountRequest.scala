package test.revolut.json

class FindAccountRequest (var uuid:String, var amounts: (BigDecimal, BigDecimal), var currencyUuid: String, var ownerUuid: String){
  def this() {this(null, null, null, null)}
  
}