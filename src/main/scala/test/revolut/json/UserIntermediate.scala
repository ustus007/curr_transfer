package test.revolut.json

import test.revolut.entity.User

class UserIntermediate (var uuid:String,var  fullName:String) {
  def this(user:User){
    this(user.uuid, user.fullName)
  }
}