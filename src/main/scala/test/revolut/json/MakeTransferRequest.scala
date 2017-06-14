package test.revolut.json

class MakeTransferRequest (var fromUuid:String, var toUuid:String,var amount:BigDecimal){
  def this(){this(null, null, null)}
  
}