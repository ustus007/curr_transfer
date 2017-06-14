package test.revolut.json

class CreateAccountRequest (var currencyUuid: String, var ownerUuid: String) {
  def this() {this(null, null)}
}