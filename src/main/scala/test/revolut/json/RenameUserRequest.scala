package test.revolut.json

class RenameUserRequest (var uuid:String, var fullName: String) {
  def this() {this(null,null)}
}