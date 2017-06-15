package test.revolut.DAO.impl

import org.scalatest.FlatSpec
import test.revolut.utilities.TestUtils._
import test.revolut.DAO.CurrencyDAO
import test.revolut.data.storage.InMemory.storage
import test.revolut.entity.Currency
import org.scalatest.BeforeAndAfterEach
import test.revolut.DAO.DAOStorage

class CurrencyDAOImplTest extends FlatSpec with BeforeAndAfterEach  {
  
  val currencyDAO:CurrencyDAO = DAOStorage.getOrCreate("currencyDAO", () => { new CurrencyDAOImpl }).asInstanceOf[CurrencyDAO]
  
  override def beforeEach() {
     restoreStorage
   }
  
  "CurrencyDAOImpl" should "be able to create specified Currency" in {
    val short = genString(3).toUpperCase
    val full = genString(10)
    val created = currencyDAO.createCurrency(short, full)
    assert(created.shortName.equals(short))
    assert(created.fullName.equals(full))
    assert(storage.currencies.filter { x => x.uuid.equals(created.uuid) }.size>0)
  }
  
  it should "be able to perform correct searches " in {
    val created = Array[Currency](null, null, null)
    for (i:Int <- 0 to 2){
      created(i) = currencyDAO.createCurrency(genString(3).toUpperCase+i, genString(10)+i)
    }
    val all = currencyDAO.readCurrency(None, None, None)
    assert(all.size === 3 )
    assert(all.filter { x => x.uuid.equals(created(0).uuid) }.size === 1)
    assert(all.filter { x => x.uuid.equals(created(1).uuid) }.size === 1)
    assert(all.filter { x => x.uuid.equals(created(2).uuid) }.size === 1)
    val byId = currencyDAO.readCurrency(Some(List(created(0).uuid,created(1).uuid)), None, None)
    assert(byId.size === 2 )
    assert(byId.filter { x => x.uuid.equals(created(0).uuid) }.size === 1)
    assert(byId.filter { x => x.uuid.equals(created(1).uuid) }.size === 1)
    val byShort = currencyDAO.readCurrency(None, Some(List(created(0).shortName, created(2).shortName)), None)
    assert(byShort.filter { x => x.uuid.equals(created(0).uuid) }.size === 1)
    assert(byShort.filter { x => x.uuid.equals(created(2).uuid) }.size === 1)
    val byFull = currencyDAO.readCurrency(None, None, Some( List( created(1).fullName, created(2).fullName ) ))
    assert(byFull.filter { x => x.uuid.equals(created(1).uuid) }.size === 1)
    assert(byFull.filter { x => x.uuid.equals(created(2).uuid) }.size === 1)
  }
  
  it should "be able to correctly rename currencies" in {
    val created = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
    val changed = currencyDAO.updateCurrency(created.uuid, Some(genString(3).toUpperCase), Some(genString(10))).get
    val stored = storage.currencies.filter { x => x.uuid.equals(created.uuid) }.last
    assert(stored.uuid.equals(changed.uuid))
    assert(stored.shortName.equals(changed.shortName))
    assert(stored.fullName.equals(changed.fullName))
    val other = currencyDAO.updateCurrency(created.uuid+"0", None, None)
    assert(other == None)    
  }
  
  it should "be able to correctly delete currencies" in {
    val created = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
    val wrongTry = currencyDAO.deleteCurrency("")
    assert(!wrongTry)
    val correctTry = currencyDAO.deleteCurrency(created.uuid)
    assert(correctTry)
    assert(storage.currencies.filter { x => x.uuid.equals(created.uuid) }.size === 0)
  }
  
}