package test.revolut.json

import test.revolut.entity.Account

class AccountIntermediate (var uuid: String,var amount: BigDecimal, var currencyID:String, var currencyName: String){
  def this(account:Account){
    this(account.uuid, account.amount, account.currency.uuid, account.currency.fullName + " ("+account.currency.shortName+")")
  }
}