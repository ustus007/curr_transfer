package test.revolut.DAO.impl

import test.revolut.DAO.AccountDAO
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FlatSpec
import test.revolut.utils.TestUtils._
import test.revolut.DAO.UserDAO
import test.revolut.DAO.CurrencyDAO
import test.revolut.data.storage.InMemory.storage
import test.revolut.DAO.DAOStorage
import test.revolut.entity.Currency
import test.revolut.entity.User
import test.revolut.entity.Account

class AccountDAOImplTest extends FlatSpec with BeforeAndAfterEach {

  val accountDAO: AccountDAO = DAOStorage.getOrCreate("accountDAO", () => { new AccountDAOImpl }).asInstanceOf[AccountDAO]
  val currencyDAO: CurrencyDAO = DAOStorage.getOrCreate("currencyDAO", () => { new CurrencyDAOImpl }).asInstanceOf[CurrencyDAO]
  val userDAO: UserDAO = DAOStorage.getOrCreate("userDAO", () => { new UserDAOImpl }).asInstanceOf[UserDAO]

  override def beforeEach() {
    restoreStorage
  }

  "UserDAOImpl" should "be able to create specified Account" in {

    val cur = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
    val usr = userDAO.createUser(genString(12))
    val account = accountDAO.createAccount(cur, usr)
    assert(account.currency.uuid.equals(cur.uuid))
    assert(account.user.uuid.equals(usr.uuid))
    assert(storage.accounts.filter { x => x.uuid.equals(account.uuid) }.size == 1)

  }

  it should "be able to perform correct searches " in {
    val cur = Array[Currency](null, null, null, null)
    val usr = Array[User](null, null, null, null)
    val acs = Array[Account](null, null, null, null)
    val amt = Array[BigDecimal](null, null, null, null)
    for (i: Int <- 0 to 3) {
      cur(i) = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
      usr(i) = userDAO.createUser(genString(12))
      acs(i) = accountDAO.createAccount(cur(i), usr(i))
      amt(i) = genPosBigDec
      acs(i) = accountDAO.updateAccount(acs(i).uuid, Some(amt(i))).get

    }
    val all = accountDAO.readAccount(None, None, None, None)
    assert(all.size === 4)
    for (i: Int <- 0 to 3) {
      assert(all.filter { x => x.uuid.equals(acs(i).uuid) }.size === 1)
    }
    val byUuid = accountDAO.readAccount(Some(List(acs(0).uuid, acs(1).uuid, acs(2).uuid)), None, None, None)
    assert(byUuid.size === 3)
    for (i: Int <- 0 to 2) {
      assert(byUuid.filter { x => x.uuid.equals(acs(i).uuid) }.size === 1)
    }
    val byAmount = accountDAO.readAccount(None,
      Some(List((BigDecimal(0), acs(1).amount), (BigDecimal(0), acs(2).amount), (BigDecimal(0), acs(3).amount))),
      None, None)
    assert(byAmount.size >= 3) //Because amount ranges could include each other
    for (i: Int <- 1 to 3) {
      assert(byAmount.filter { x => x.uuid.equals(acs(i).uuid) }.size === 1)
    }
    val byCurrency = accountDAO.readAccount(None, None, Some(List(cur(0), cur(2), cur(3))), None)
    assert(byCurrency.size === 3)
    for (i: Int <- 0 to 3) {
      if (i != 1) {
        assert(byCurrency.filter { x => x.uuid.equals(acs(i).uuid) }.size === 1)
      }
    }
    val byUsers = accountDAO.readAccount(None, None, None, Some(List(usr(0), usr(1), usr(3))))
    for (i: Int <- 0 to 3) {
      if (i != 2) {
        assert(byUsers.filter { x => x.uuid.equals(acs(i).uuid) }.size === 1)
      }
    }
  }

  it should "be able to correctly update amounts on accounts" in {
    val cur = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
    val usr = userDAO.createUser(genString(12))
    val account = accountDAO.createAccount(cur, usr)
    val changed = accountDAO.updateAccount(account.uuid, Some(genPosBigDec)).get
    val stored = storage.accounts.filter { x => x.uuid.equals(account.uuid) }.last
    assert(stored.uuid.equals(changed.uuid))
    assert(stored.amount.equals(changed.amount))
    val other = accountDAO.updateAccount(account.uuid + "0", None)
    assert(other == None)
  }

  it should "be able to correctly delete accounts" in {
    val cur = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
    val usr = userDAO.createUser(genString(12))
    val created = accountDAO.createAccount(cur, usr)
    val oldUsr = userDAO.readUser(Some(List(usr.uuid)), None).last
    assert(oldUsr.accounts.size === 1)
    val wrongTry = accountDAO.deleteAccount("", true)
    assert(!wrongTry)
    val correctTry = accountDAO.deleteAccount(created.uuid, true)
    assert(correctTry)
    val newUsr = userDAO.readUser(Some(List(usr.uuid)), None).last
    assert(newUsr.accounts.size === 0)
    assert(storage.accounts.filter { x => x.uuid.equals(created.uuid) }.size === 0)
  }

   it should "delete accounts in users records only if appropriate option was selected" in {
     val cur = currencyDAO.createCurrency(genString(3).toUpperCase, genString(10))
    val usr = userDAO.createUser(genString(12))
    val created = accountDAO.createAccount(cur, usr)
    val oldUsr = userDAO.readUser(Some(List(usr.uuid)), None).last
    assert(oldUsr.accounts.size === 1)
    val correctTry = accountDAO.deleteAccount(created.uuid, false)
    assert(correctTry)
    val newUsr = userDAO.readUser(Some(List(usr.uuid)), None).last
    assert(newUsr.accounts.size === 1)
    assert(storage.accounts.filter { x => x.uuid.equals(created.uuid) }.size === 0)
   }
  
}