package test.revolut.manager

import test.revolut.entity.Currency

trait CurrencyManager extends AbstractManager {
  
  def addCurrency(shortName:String, fullName: String):Currency
  def findCurrency(uuid:Option[String], shortName: Option[String], fullName:Option[String]):List[Currency]
  def renameCurrency(uuid:String, shortName:Option[String], fullName: Option[String]):Option[Currency]
  def deleteCurrency(uuid:String):Boolean

}