package test.revolut.manager.impl

import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FlatSpec

import test.revolut.DAO.AccountDAO
import test.revolut.DAO.TransferDAO
import test.revolut.entity.Account
import test.revolut.entity.Currency
import test.revolut.entity.User
import test.revolut.utilities.TestUtils.genString
import test.revolut.utilities.Util.generateUuid
import test.revolut.exceptions.IncorrectSumException
import test.revolut.entity.Transfer
import test.revolut.exceptions.NotEnoughBallanceException

class AccountManagerImplTest extends FlatSpec with BeforeAndAfterEach with MockFactory {

  val accountManager: AccountManagerImpl = new AccountManagerImpl
  val admock = mock[AccountDAO]
  val tdmock = mock[TransferDAO]
  accountManager.accountDAO = admock
  accountManager.transferDAO = tdmock

  "AccountManagerImpl" should "be able to create Account" in {
    val name = genString(12)
    val usr = new User(generateUuid, name, Set.empty[Account])
    val short = genString(3).toUpperCase
    val full = genString(10)
    val cr = Currency(generateUuid, short, full)
    val acc = Account(generateUuid, cr, BigDecimal(0), usr)
    (admock.createAccount _).expects(cr, usr).returns(acc)
    val test = accountManager.createAccount(cr, usr)
    assert(acc === test)
  }
  it should "be able to perform correct searches " in {
    val name = genString(12)
    val usr = new User(generateUuid, name, Set.empty[Account])
    val short = genString(3).toUpperCase
    val full = genString(10)
    val cr = Currency(generateUuid, short, full)
    val acc = Account(generateUuid, cr, BigDecimal(1), usr)
    (admock.readAccount _).expects(None, None, None, None).returns(List.empty[Account])
    val t1 = accountManager.findAccount(None, None, None, None)
    assert(t1 == List.empty[Account])
    (admock.readAccount _).expects(Some(List(acc.uuid)), Some(List((BigDecimal(0), BigDecimal(2)))), Some(List(cr)), Some(List(usr))).returns(List(acc))
    val t2 = accountManager.findAccount(Some(acc.uuid), Some((BigDecimal(0), BigDecimal(2))), Some(cr), Some(usr)).last
    assert(t2 === acc)
  }

  it should "be able to top up accounts" in {
    val name = genString(12)
    val usr = new User(generateUuid, name, Set.empty[Account])
    val short = genString(3).toUpperCase
    val full = genString(10)
    val cr = Currency(generateUuid, short, full)
    val acc = Account(generateUuid, cr, BigDecimal(1), usr)
    assertThrows[IncorrectSumException](accountManager.topUpAccountFromOutside(acc.uuid, BigDecimal(-1)))
    (admock.readAccount _).expects(Some(List(acc.uuid)), None, None, None).returns(List.empty[Account])
    val t1 = accountManager.topUpAccountFromOutside(acc.uuid, BigDecimal(1))
    assert(t1 == None)
    (admock.readAccount _).expects(Some(List(acc.uuid)), None, None, None).returns(List(acc))
    (tdmock.readTransfer _).expects(None, Some(List(acc)), Some(List(acc)), None, None, None).returns(List.empty[Transfer])
    (admock.updateAccount _).expects(acc.uuid, Some(acc.amount + BigDecimal(1))).returns(Some(acc))
    val t2 = accountManager.topUpAccountFromOutside(acc.uuid, BigDecimal(1)).get
    assert(acc === t2)
  }

  it should "be able to withdraw from accounts" in {
    val name = genString(12)
    val usr = new User(generateUuid, name, Set.empty[Account])
    val short = genString(3).toUpperCase
    val full = genString(10)
    val cr = Currency(generateUuid, short, full)
    val acc = Account(generateUuid, cr, BigDecimal(5), usr)
    assertThrows[IncorrectSumException](accountManager.withdrawFromAccountOutside(acc.uuid, BigDecimal(-1)))
    (admock.readAccount _).expects(Some(List(acc.uuid)), None, None, None).returns(List.empty[Account])
    val t1 = accountManager.withdrawFromAccountOutside(acc.uuid, BigDecimal(1))
    assert(t1 == None)
    (admock.readAccount _).expects(Some(List(acc.uuid)), None, None, None).returns(List(acc))
    assertThrows[NotEnoughBallanceException](accountManager.withdrawFromAccountOutside(acc.uuid, BigDecimal(10)))
    (admock.readAccount _).expects(Some(List(acc.uuid)), None, None, None).returns(List(acc))
    (tdmock.readTransfer _).expects(None, Some(List(acc)), Some(List(acc)), None, None, None).returns(List.empty[Transfer])
    (admock.updateAccount _).expects(acc.uuid, Some(acc.amount - BigDecimal(1))).returns(Some(acc))
    val t2 = accountManager.withdrawFromAccountOutside(acc.uuid, BigDecimal(1)).get
    assert(acc === t2)
  }
  
  it should "be able to delete account" in {
    val name = genString(12)
    val usr = new User(generateUuid, name, Set.empty[Account])
    val short = genString(3).toUpperCase
    val full = genString(10)
    val cr = Currency(generateUuid, short, full)
    val acc = Account(generateUuid, cr, BigDecimal(5), usr)
    (admock.readAccount _).expects(Some(List(acc.uuid)), None, None, None).returns(List.empty[Account])
    val t1 = accountManager.deleteAccount(acc.uuid)
    assert(!t1)
    (admock.readAccount _).expects(Some(List(acc.uuid)), None, None, None).returns(List(acc))
    (tdmock.readTransfer _).expects(None, Some(List(acc)), Some(List(acc)), None, None, None).returns(List.empty[Transfer])
    (admock.deleteAccount _).expects(acc.uuid,true).returns(true)
    val t2 = accountManager.deleteAccount(acc.uuid)
    assert(t2)
    
  }
}