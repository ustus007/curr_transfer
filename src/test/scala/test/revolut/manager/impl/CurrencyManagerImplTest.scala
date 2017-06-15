package test.revolut.manager.impl

import org.scalatest.BeforeAndAfterEach
import org.scalatest.FlatSpec
import test.revolut.utilities.TestUtils._
import test.revolut.utilities.Util._
import test.revolut.manager.impl.CurrencyManagerImpl
import org.scalamock.scalatest.MockFactory
import test.revolut.DAO.CurrencyDAO
import test.revolut.entity.Currency
import test.revolut.DAO.AccountDAO
import test.revolut.entity.User
import test.revolut.entity.Account
import scala.BigDecimal

class CurrencyManagerImplTest  extends FlatSpec with BeforeAndAfterEach with MockFactory  {
  
  val currencyManager:CurrencyManagerImpl = new CurrencyManagerImpl
  val cdmock = mock[CurrencyDAO]
  val admock = mock[AccountDAO]
  currencyManager.currencyDAO = cdmock
  currencyManager.accountDAO=admock
  
  "CurrencyManagerImpl" should "be able to create specified Currency" in {
    val short = genString(3).toUpperCase
    val full = genString(10)
    (cdmock.createCurrency _).expects(short, full).returns(Currency(generateUuid, short, full))
    val cur = currencyManager.addCurrency(short, full)
    assert(cur.fullName.equals(full))
    assert(cur.shortName.equals(short))
  }
  
  it should "be able to perform correct searches " in {
    (cdmock.readCurrency _).expects(None, None, None).returns(List.empty[Currency])
    val cur = currencyManager.findCurrency(None, None, None)
    assert(cur == List.empty[Currency])
    val short = genString(3).toUpperCase
    val full = genString(10)
    val uuid = generateUuid
    val cr = Currency(generateUuid, short, full)
    (cdmock.readCurrency _).expects(Some(List(uuid)), Some(List(short)), Some(List(full))).returns(List(cr))
    val cr2 = currencyManager.findCurrency(Some(uuid), Some(short), Some(full)).last
    assert(cr.uuid.equals(cr2.uuid))
    assert(cr.fullName.equals(cr2.fullName))
    assert(cr.shortName.equals(cr2.shortName))
  }
  
  it should "be able to rename currencies " in {
    val short = genString(3).toUpperCase
    val full = genString(10)
    val uuid = generateUuid
    val cr = Currency(generateUuid, short, full)
    (cdmock.updateCurrency _).expects(uuid, Some(short), Some(full)).returns(Some(cr))
    val cr2 = currencyManager.renameCurrency(uuid, Some(short), Some(full)).get
    assert(cr.uuid.equals(cr2.uuid))
    assert(cr.fullName.equals(cr2.fullName))
    assert(cr.shortName.equals(cr2.shortName))
  }
  
  it should "be able to delete currencies " in {
    val uuid = generateUuid
    (cdmock.readCurrency _).expects(Some(List(uuid)), None, None).returns(List.empty[Currency])
    val try1 = currencyManager.deleteCurrency(uuid)
    assert(!try1)
    val short = genString(3).toUpperCase
    val full = genString(10)
    val cr = Currency(generateUuid, short, full)
    val usr = User(generateUuid, genString(12), Set.empty[Account])
    val acc = Account(generateUuid, cr, BigDecimal(0), usr)
    usr.accounts = Set(acc)
    (cdmock.readCurrency _).expects(Some(List(uuid)), None, None).returns(List(cr))
    (admock.readAccount _).expects(None, None, Some(List(cr)), None).returns(List(acc))
    (cdmock.deleteCurrency _).expects(uuid).returns(true)
    val try2 = currencyManager.deleteCurrency(uuid)
    assert(try2)
    
  }
  
}