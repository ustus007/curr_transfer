package test.revolut.DAO.impl

import org.scalatest.FlatSpec
import test.revolut.utils.TestUtils._
import test.revolut.DAO.CurrencyDAO
import test.revolut.data.storage.InMemory.storage
import test.revolut.entity.Currency

class CurrencyDAOImplTest extends FlatSpec {
  
  val currencyDAO:CurrencyDAO = new CurrencyDAOImpl
  
  "CurrencyDAOImpl" should "be able to create specified Currency" in {
    val short = genString(3).toUpperCase
    val full = genString(10)
    val created = currencyDAO.createCurrency(short, full)
    assert(created.shortName.equals(short))
    assert(created.fullName.equals(full))
    assert(storage.currencies.filter { x => x.uuid.equals(created.uuid) }.size>0)
    restoreStorage
  }
  
  it should "be able to perform correct searches " in {
    val shorts = Array[String](genString(3).toUpperCase, genString(3).toUpperCase, genString(3).toUpperCase)
    val fulls = Array[String](genString(10), genString(10), genString(10))
    val created = Array[Currency](null, null, null)
    for (i:Int <- 0 to 2){
      created(i) = currencyDAO.createCurrency(shorts(i), fulls(i))
    }
    
  }
  
}