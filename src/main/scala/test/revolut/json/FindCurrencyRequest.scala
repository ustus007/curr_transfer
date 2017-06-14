package test.revolut.json

class FindCurrencyRequest (var uuid: String, var shortName: String, var fullName: String){
  def this() {this(null, null, null)}
}