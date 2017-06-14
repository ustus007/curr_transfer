package test.revolut.json

class FindUserRequest (var uuid:String, var fullName: String) {
  def this() {this(null,null)}
}