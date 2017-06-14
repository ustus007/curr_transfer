package test.revolut.json

import test.revolut.entity.User

class UserSearchResponse (var uuid:String,var  fullName:String,var  accounts: List[AccountIntermediate]) {
  def this(user:User){
    this(user.uuid, user.fullName, user.accounts.map { x => new AccountIntermediate(x) }.toList)
  }
}