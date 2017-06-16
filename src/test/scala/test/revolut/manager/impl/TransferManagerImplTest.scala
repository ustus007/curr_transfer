package test.revolut.manager.impl

import org.scalatest.BeforeAndAfterEach
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import test.revolut.utilities.TestUtils._
import test.revolut.utilities.Util._
import test.revolut.DAO.TransferDAO
import test.revolut.DAO.AccountDAO
import test.revolut.entity.Account
import test.revolut.entity.User
import test.revolut.entity.Currency
import test.revolut.exceptions.AccountNotFoundException
import test.revolut.exceptions.DifferentCurrenciesException
import test.revolut.exceptions.NotEnoughBallanceException
import java.time.LocalDateTime
import test.revolut.entity.Transfer
import test.revolut.exceptions.TransferNotFoundException
import test.revolut.exceptions.TransferAlreadyRolledBackException

class TransferManagerImplTest extends FlatSpec with BeforeAndAfterEach with MockFactory {

  val transferManager: TransferManagerImpl = new TransferManagerImpl
  val admock = mock[AccountDAO]
  val tdmock = mock[TransferDAO]
  transferManager.accountDAO = admock
  transferManager.transferDAO = tdmock

  "TransferManagerImpl" should "be able to make transfer" in {
    val name1 = genString(12)
    val usr1 = new User(generateUuid, name1, Set.empty[Account])
    val short1 = genString(3).toUpperCase
    val full1 = genString(10)
    val cr1 = Currency(generateUuid, short1, full1)
    val from = Account(generateUuid, cr1, BigDecimal(10), usr1)
    val name2 = genString(12)
    val usr2 = new User(generateUuid, name2, Set.empty[Account])
    val short2 = genString(3).toUpperCase
    val full2 = genString(10)
    val cr2 = Currency(generateUuid, short2, full1)
    var to = Account(generateUuid, cr2, BigDecimal(20), usr2)
    (admock.readAccount _).expects(Some(List(from.uuid)), None, None, None).returns(List.empty[Account])
    (admock.readAccount _).expects(Some(List(to.uuid)), None, None, None).returns(List.empty[Account])
    assertThrows[AccountNotFoundException](transferManager.makeTransfer(from, to, BigDecimal(5)))
    (admock.readAccount _).expects(Some(List(from.uuid)), None, None, None).returns(List(from))
    (admock.readAccount _).expects(Some(List(to.uuid)), None, None, None).returns(List.empty[Account])
    assertThrows[AccountNotFoundException](transferManager.makeTransfer(from, to, BigDecimal(5)))
    (admock.readAccount _).expects(Some(List(from.uuid)), None, None, None).returns(List(from))
    (admock.readAccount _).expects(Some(List(to.uuid)), None, None, None).returns(List(to))
    assertThrows[DifferentCurrenciesException](transferManager.makeTransfer(from, to, BigDecimal(5)))
    to = Account(generateUuid, cr1, BigDecimal(20), usr2)
    (admock.readAccount _).expects(Some(List(from.uuid)), None, None, None).returns(List(from))
    (admock.readAccount _).expects(Some(List(to.uuid)), None, None, None).returns(List(to))
    assertThrows[NotEnoughBallanceException](transferManager.makeTransfer(from, to, BigDecimal(100)))
    (admock.readAccount _).expects(Some(List(from.uuid)), None, None, None).returns(List(from))
    (admock.readAccount _).expects(Some(List(to.uuid)), None, None, None).returns(List(to))
    (admock.updateAccount _).expects(from.uuid, Some(from.amount - BigDecimal(5))).returns(Some(from))
    (admock.updateAccount _).expects(to.uuid, Some(to.amount + BigDecimal(5))).returns(Some(to))
    (tdmock.createTransfer _).expects(from, to, BigDecimal(5)).returns(Transfer(generateUuid, from, to, BigDecimal(5), LocalDateTime.now(), false))
    val trs = transferManager.makeTransfer(from, to, BigDecimal(5))
    assert(trs.from === from)
    assert(trs.to === to)
    assert(trs.amount == BigDecimal(5))
    assert(!trs.rolledBack)
    assert(trs.moment.plusSeconds(120).isAfter(LocalDateTime.now()))

  }

  it should "be able to rollback transfer" in {
    val name1 = genString(12)
    val usr1 = new User(generateUuid, name1, Set.empty[Account])
    val short1 = genString(3).toUpperCase
    val full1 = genString(10)
    val cr1 = Currency(generateUuid, short1, full1)
    val from = Account(generateUuid, cr1, BigDecimal(10), usr1)
    val name2 = genString(12)
    val usr2 = new User(generateUuid, name2, Set.empty[Account])
    var to = Account(generateUuid, cr1, BigDecimal(20), usr2)
    var trs = Transfer(generateUuid, from, to, BigDecimal(5), LocalDateTime.now(), false)
    (tdmock.readTransfer _).expects(Some(List(trs.uuid)), None, None, None, None, None).returns(List.empty[Transfer])
    assertThrows[TransferNotFoundException](transferManager.rollbackTransfer(trs.uuid))
    trs.rolledBack = true
    (tdmock.readTransfer _).expects(Some(List(trs.uuid)), None, None, None, None, None).returns(List(trs))
    assertThrows[TransferAlreadyRolledBackException](transferManager.rollbackTransfer(trs.uuid))
    to = Account(generateUuid, cr1, BigDecimal(1), usr2)
    trs = Transfer(generateUuid, from, to, BigDecimal(5), LocalDateTime.now(), false)
    (tdmock.readTransfer _).expects(Some(List(trs.uuid)), None, None, None, None, None).returns(List(trs))
    assertThrows[NotEnoughBallanceException](transferManager.rollbackTransfer(trs.uuid))
    to = Account(generateUuid, cr1, BigDecimal(10), usr2)
    trs = Transfer(generateUuid, from, to, BigDecimal(5), LocalDateTime.now(), false)
    (tdmock.readTransfer _).expects(Some(List(trs.uuid)), None, None, None, None, None).returns(List(trs))
    (admock.updateAccount _).expects(to.uuid, Some(to.amount - trs.amount))
    (admock.updateAccount _).expects(from.uuid, Some(from.amount + trs.amount))
    (tdmock.updateTransfer _).expects(trs.uuid, Some(true)).returns(Some(trs))
    val result = transferManager.rollbackTransfer(trs.uuid)
    assert(result)

  }

  it should "be able to perform correct searches " in {
    val name1 = genString(12)
    val usr1 = new User(generateUuid, name1, Set.empty[Account])
    val short1 = genString(3).toUpperCase
    val full1 = genString(10)
    val cr1 = Currency(generateUuid, short1, full1)
    val from = Account(generateUuid, cr1, BigDecimal(10), usr1)
    val name2 = genString(12)
    val usr2 = new User(generateUuid, name2, Set.empty[Account])
    val to = Account(generateUuid, cr1, BigDecimal(20), usr2)
    val timerange = (LocalDateTime.now().minusSeconds(120), LocalDateTime.now().plusSeconds(120))
    val trs = Transfer(generateUuid, from, to, BigDecimal(5), LocalDateTime.now(), false)
    (tdmock.readTransfer _).expects(Some(List(trs.uuid)), Some(List(from)), Some(List(to)), Some(List((BigDecimal(5), BigDecimal(25)))),
      Some(List(timerange)), Some(List(false))).returns(List(trs))
    val result = transferManager.findTransfers(Some(List(trs.uuid)), Some(List(from)), Some(List(to)), Some(List((BigDecimal(5), BigDecimal(25)))),
      Some(List(timerange)), Some(List(false)))
    assert(result.last === trs)
  }

  it should "be able to delete transfer" in {
    val name1 = genString(12)
    val usr1 = new User(generateUuid, name1, Set.empty[Account])
    val short1 = genString(3).toUpperCase
    val full1 = genString(10)
    val cr1 = Currency(generateUuid, short1, full1)
    val from = Account(generateUuid, cr1, BigDecimal(10), usr1)
    val name2 = genString(12)
    val usr2 = new User(generateUuid, name2, Set.empty[Account])
    val to = Account(generateUuid, cr1, BigDecimal(20), usr2)
    val trs = Transfer(generateUuid, from, to, BigDecimal(5), LocalDateTime.now(), false)
    (tdmock.readTransfer _).expects(Some(List(trs.uuid)), None, None, None, None, None).returns(List.empty[Transfer])
    assertThrows[TransferNotFoundException](transferManager.rollbackTransfer(trs.uuid))
    (tdmock.readTransfer _).expects(Some(List(trs.uuid)), None, None, None, None, None).returns(List(trs))
    (tdmock.deleteTransfer _).expects(trs.uuid).returns(true)
    val result = transferManager.deleteTransfer(trs.uuid)
    assert(result)

  }

}
