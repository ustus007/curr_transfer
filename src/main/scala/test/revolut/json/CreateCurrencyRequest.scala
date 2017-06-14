package test.revolut.json

class CreateCurrencyRequest (var shortName: String, var fullName: String) {
  def this() {this(null, null)}
}