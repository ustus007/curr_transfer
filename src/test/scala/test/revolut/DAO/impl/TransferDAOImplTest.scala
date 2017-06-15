package test.revolut.DAO.impl

import org.scalatest.FlatSpec
import test.revolut.utils.TestUtils._
import org.scalatest.BeforeAndAfterEach
import test.revolut.data.storage.InMemory.storage
import test.revolut.DAO.UserDAO
import test.revolut.DAO.CurrencyDAO
import test.revolut.DAO.DAOStorage
import test.revolut.DAO.AccountDAO
import test.revolut.DAO.TransferDAO
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import test.revolut.entity.Currency
import test.revolut.entity.User
import test.revolut.entity.Account
import test.revolut.entity.Transfer
import scala.util.Random

class TransferDAOImplTest extends FlatSpec with BeforeAndAfterEach {

  val accountDAO: AccountDAO = DAOStorage.getOrCreate("accountDAO", () => { new AccountDAOImpl }).asInstanceOf[AccountDAO]
  val currencyDAO: CurrencyDAO = DAOStorage.getOrCreate("currencyDAO", () => { new CurrencyDAOImpl }).asInstanceOf[CurrencyDAO]
  val userDAO: UserDAO = DAOStorage.getOrCreate("userDAO", () => { new UserDAOImpl }).asInstanceOf[UserDAO]
  val transferDAO: TransferDAO = DAOStorage.getOrCreate("transferDAO", () => { new TransferDAOImpl }).asInstanceOf[TransferDAO]

  override def beforeEach() {
    restoreStorage
  }

  "TransferDAOImpl" should "be able to create specified Transfer" in {
    val cur1 = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
    val usr1 = userDAO.createUser(genString(12))
    val acFrom = accountDAO.createAccount(cur1, usr1)
    val cur2 = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
    val usr2 = userDAO.createUser(genString(12))
    val acTo = accountDAO.createAccount(cur2, usr2)
    val created = transferDAO.createTransfer(acFrom, acTo, genPosBigDec)
    assert(storage.transfers.filter { x => x.uuid.equals(created.uuid) }.size == 1)
    assert(!created.rolledBack)
    assert(created.from.uuid.equals(acFrom.uuid))
    assert(created.to.uuid.equals(acTo.uuid))
    assert(created.moment.until(LocalDateTime.now, ChronoUnit.SECONDS) < 120) //just in case of long testing...
  }

  it should "be able to perform correct Transfer searches " in {
    val cur1 = Array[Currency](null, null, null, null, null, null)
    val cur2 = Array[Currency](null, null, null, null, null, null)
    val usr1 = Array[User](null, null, null, null, null, null)
    val usr2 = Array[User](null, null, null, null, null, null)
    val acFrom = Array[Account](null, null, null, null, null, null)
    val acTo = Array[Account](null, null, null, null, null, null)
    val created = Array[Transfer](null, null, null, null, null, null)
    for (i: Int <- 0 to 5) {
      cur1(i) = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
      usr1(i) = userDAO.createUser(genString(12))
      acFrom(i) = accountDAO.createAccount(cur1(i), usr1(i))
      cur2(i) = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
      usr2(i) = userDAO.createUser(genString(12))
      acTo(i) = accountDAO.createAccount(cur2(i), usr2(i))
      created(i) = transferDAO.createTransfer(acFrom(i), acTo(i), genPosBigDec)
      created(i) = transferDAO.updateTransfer(created(i).uuid, Some(Random.nextInt() % 2 == 0)).get
    }
    val all = transferDAO.readTransfer(None, None, None, None, None, None)
    assert(all.size === 6)
    for (i: Int <- 0 to 5) {
      assert(all.filter { x => x.uuid.equals(created(i).uuid) }.size === 1)
    }
    val byId = transferDAO.readTransfer(Some(created.toList.filter { x => !x.uuid.equals(created(0).uuid) }.map { x => x.uuid }), None, None, None, None, None)
    assert(byId.size === 5)
    for (i: Int <- 1 to 5) {
      assert(byId.filter { x => x.uuid.equals(created(i).uuid) }.size === 1)
    }
    val byFrom = transferDAO.readTransfer(None, Some(created.toList.filter { x => !x.uuid.equals(created(1).uuid) }.map { x => x.from }), None, None, None, None)
    assert(byFrom.size === 5)
    for (i: Int <- 0 to 5) {
      if (i != 1) {
        assert(byFrom.filter { x => x.uuid.equals(created(i).uuid) }.size === 1)
      }
    }
    val byTo = transferDAO.readTransfer(None, None, Some(created.toList.filter { x => !x.uuid.equals(created(2).uuid) }.map { x => x.to }), None, None, None)
    assert(byTo.size === 5)
    for (i: Int <- 0 to 5) {
      if (i != 2) {
        assert(byTo.filter { x => x.uuid.equals(created(i).uuid) }.size === 1)
      }
    }
    val byAmount = transferDAO.readTransfer(None, None, None, Some(created.toList.filter { x => !x.uuid.equals(created(3).uuid) }.map { x => (x.amount - BigDecimal(1), x.amount + BigDecimal(1)) }), None, None)
    assert(byTo.size >= 5) //again, in case overlapping intervals
    for (i: Int <- 0 to 5) {
      if (i != 3) {
        assert(byAmount.filter { x => x.uuid.equals(created(i).uuid) }.size === 1)
      }
    }
    val byTime = transferDAO.readTransfer(None, None, None, None, Some(created.toList.filter { x => !x.uuid.equals(created(4).uuid) }.map { x => (x.moment.minusSeconds(10), x.moment.plusSeconds(10)) }), None)
    assert(byTime.size >= 5) //again, in case overlapping intervals
    for (i: Int <- 0 to 5) {
      if (i != 4) {
        assert(byTime.filter { x => x.uuid.equals(created(i).uuid) }.size === 1)
      }
    }
    val byRb = transferDAO.readTransfer(None, None, None, None, None, Some(created.toList.filter { x => !x.uuid.equals(created(4).uuid) }.map { x => x.rolledBack }))
    assert(byTime.size >= 5) //only true or false, will overlap almost for sure
    for (i: Int <- 0 to 4) {
      assert(byTime.filter { x => x.uuid.equals(created(i).uuid) }.size === 1)
    }
  }

  it should "be able to correctly update rollback status on transfers" in {
    val cur1 = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
    val usr1 = userDAO.createUser(genString(12))
    val acFrom = accountDAO.createAccount(cur1, usr1)
    val cur2 = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
    val usr2 = userDAO.createUser(genString(12))
    val acTo = accountDAO.createAccount(cur2, usr2)
    val created = transferDAO.createTransfer(acFrom, acTo, genPosBigDec)
    val changed = transferDAO.updateTransfer(created.uuid, Some(true)).get
    val stored = storage.transfers.filter { x => x.uuid.equals(created.uuid) }.last
    assert(stored.uuid.equals(changed.uuid))
    assert(stored.rolledBack===changed.rolledBack)
    val other = transferDAO.updateTransfer(created.uuid + "0", Some(true))
    assert(other == None)
  }
  
  it should "be able to correctly delete transfers" in {
    val cur1 = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
    val usr1 = userDAO.createUser(genString(12))
    val acFrom = accountDAO.createAccount(cur1, usr1)
    val cur2 = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
    val usr2 = userDAO.createUser(genString(12))
    val acTo = accountDAO.createAccount(cur2, usr2)
    val created = transferDAO.createTransfer(acFrom, acTo, genPosBigDec)
    val wrongTry = transferDAO.deleteTransfer("")
    assert(!wrongTry)
    val correctTry = transferDAO.deleteTransfer(created.uuid)
    assert(correctTry)
    assert(storage.transfers.filter { x => x.uuid.equals(created.uuid) }.size === 0)
  }
}