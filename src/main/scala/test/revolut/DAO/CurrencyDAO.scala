package test.revolut.DAO

import test.revolut.data.storage.{ InMemory => st }
import test.revolut.{ entity => en }

trait CurrencyDAO extends AbstractDAO {
  
   def transformCurrency(internal: st.Currency): en.Currency
   def createCurrency(shortName: String, fullName:String):en.Currency
   def readCurrency(uuids: Option[List[String]], shortNames: Option[List[String]], fullNames: Option[List[String]]):List[en.Currency]
   def updateCurrency(uuid:String, shortName:Option[String], fullName:Option[String]):Option[en.Currency]
   def deleteCurrency(uuid: String): Boolean
}