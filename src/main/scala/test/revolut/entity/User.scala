package test.revolut.entity

case class User(uuid:String, fullName: String, var accounts:Set[Account])extends Entity {
  
}