package test.revolut.DAO.impl

import org.scalatest.FlatSpec
import test.revolut.utilities.TestUtils._
import org.scalatest.BeforeAndAfterEach
import test.revolut.data.storage.InMemory.storage
import test.revolut.DAO.UserDAO
import test.revolut.DAO.AccountDAO
import test.revolut.DAO.CurrencyDAO
import test.revolut.entity.Currency
import test.revolut.entity.Account
import test.revolut.DAO.DAOStorage

class UserDAOImplTest extends FlatSpec with BeforeAndAfterEach {

  val userDAO: UserDAO = DAOStorage.getOrCreate("userDAO", () => { new UserDAOImpl }).asInstanceOf[UserDAO]

  override def beforeEach() {
    restoreStorage
  }

  "UserDAOImpl" should "be able to create specified User" in {
    val name = genString(12)
    val user = userDAO.createUser(name)
    assert(user.fullName.equals(name))
    assert(storage.users.filter { x => x.uuid.equals(user.uuid) }.size == 1)
  }

  it should "be able to perform correct searches " in {

    val first = userDAO.createUser(genString(12))
    var second = userDAO.createUser(genString(12))
    while(first.equals(second)){second = userDAO.createUser(genString(12))}

    val all = userDAO.readUser(None, None)
    assert(all.size === 2)
    assert(all.filter { x => x.uuid.equals(first.uuid) }.size === 1)
    assert(all.filter { x => x.uuid.equals(second.uuid) }.size === 1)

    val byId = userDAO.readUser(Some(List(first.uuid)), None)
    assert(byId.size === 1)
    assert(byId.last.uuid.equals(first.uuid))

    val byName = userDAO.readUser(None, Some(List(second.fullName)))
    assert(byName.size === 1)
    assert(byName.last.uuid.equals(second.uuid))

  }

  it should "be able to correctly rename users" in {
    val created = userDAO.createUser(genString(12))
    val changed = userDAO.updateUser(created.uuid, Some(genString(12))).get
    val stored = storage.users.filter { x => x.uuid.equals(created.uuid) }.last
    assert(stored.uuid.equals(changed.uuid))
    assert(stored.fullName.equals(changed.fullName))
    val other = userDAO.updateUser(created.uuid + "0", Some(genString(12)))
    assert(other == None)
  }

  it should "be able to correctly delete users" in {
    val created = userDAO.createUser(genString(12))
    val wrongTry = userDAO.deleteUser("")
    assert(!wrongTry)
    val correctTry = userDAO.deleteUser(created.uuid)
    assert(correctTry)
    assert(storage.users.filter { x => x.uuid.equals(created.uuid) }.size === 0)
  }
}
