package test.revolut.manager.impl

import test.revolut.manager.CurrencyManager
import test.revolut.DAO.CurrencyDAO
import test.revolut.DAO.impl.CurrencyDAOImpl
import test.revolut.entity.Currency
import test.revolut.DAO.impl.SynchronizationDAOImpl
import test.revolut.DAO.SynchronizationDAO
import test.revolut.DAO.impl.AccountDAOImpl
import test.revolut.DAO.AccountDAO
import test.revolut.DAO.DAOStorage

class CurrencyManagerImpl extends CurrencyManager{
  
  var currencyDAO:CurrencyDAO = DAOStorage.getOrCreate("currencyDAO", () => { new CurrencyDAOImpl }).asInstanceOf[CurrencyDAO]
  var syncDAO:SynchronizationDAO = new SynchronizationDAOImpl
  var accountDAO:AccountDAO = DAOStorage.getOrCreate("accountDAO", () => {new AccountDAOImpl}).asInstanceOf[AccountDAO]
  
  override def addCurrency(shortName:String, fullName: String):Currency = {
    currencyDAO.createCurrency(shortName, fullName)
  }
  
  override def findCurrency(uuid:Option[String], shortName: Option[String], fullName:Option[String]):List[Currency] = {
    currencyDAO.readCurrency(if(uuid.isEmpty) None else Some(List(uuid.get)), 
        if(shortName.isEmpty) None else Some(List(shortName.get)), 
        if(fullName.isEmpty) None else Some(List(fullName.get)))
  }
  
  override def renameCurrency(uuid:String, shortName:Option[String], fullName: Option[String]):Option[Currency] = {
    currencyDAO.updateCurrency(uuid, shortName, fullName)
  }
  
  
  override def deleteCurrency(uuid:String):Boolean ={
    val curs:List[Currency] = findCurrency(Some(uuid), None, None)
    if (curs.size == 0){    
      false
    } else {
      val cur = curs.last

      val acs = accountDAO.readAccount(None, None, Some(List(cur)), None).map{ x => x.uuid }
      val sync = syncDAO.getSyncObjectByUuids( cur.uuid :: acs)
      var result = false
      syncDAO.syncOn(sync, ()=> {
        result = currencyDAO.deleteCurrency(uuid)
      })
      
      result
    }
  }
  
}