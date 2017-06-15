package test.revolut.manager.impl

import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FlatSpec

import test.revolut.DAO.TransferDAO
import test.revolut.DAO.UserDAO
import test.revolut.entity.Account
import test.revolut.entity.User
import test.revolut.utilities.TestUtils.genString
import test.revolut.utilities.Util.generateUuid
import test.revolut.entity.Transfer

class UserManagerImplTest extends FlatSpec with BeforeAndAfterEach with MockFactory {

  val userManager: UserManagerImpl = new UserManagerImpl
  val udmock = mock[UserDAO]
  val tdmock = mock[TransferDAO]
  userManager.userDAO = udmock
  userManager.transferDAO = tdmock

  "UserManagerImpl" should "be able to create specified User" in {
    val name = genString(12)
    val usr = new User(generateUuid, name, Set.empty[Account])
    (udmock.createUser _).expects(name).returns(usr)
    val tst = userManager.createUser(name)
    assert(usr === tst)
  }

  it should "be able to perform correct searches " in {
    (udmock.readUser _).expects(None, None).returns(List.empty[User])
    val u1 = userManager.findUser(None, None)
    assert(u1 == List.empty[User])
    val name = genString(12)
    val uuid = generateUuid
    val u2 = User(uuid, name, Set.empty[Account])
    (udmock.readUser _).expects(Some(List(uuid)), Some(List(name))).returns(List(u2))
    val usr = userManager.findUser(Some(uuid), Some(name)).last
    assert(u2 === usr)
  }

  it should "be able to rename users " in {
    val uuid = generateUuid
    val name1 = genString(12)
    val name2 = genString(12)
    (udmock.readUser _).expects(Some(List(uuid)), None).returns(List.empty[User])
    val t1 = userManager.renameUser(uuid, name1)
    assert(t1 == None)
    val u1 = User(uuid, name1, Set.empty[Account])
    val u2 = User(uuid, name2, Set.empty[Account])
    (udmock.readUser _).expects(Some(List(uuid)), None).returns(List(u1))
    (udmock.updateUser _).expects(uuid, Some(name2)).returns(Some(u2))
    val t2 = userManager.renameUser(uuid, name2).get
    assert(u2 === t2)
  }

  it should "be able to delete users " in {
    val uuid = generateUuid
    val name = genString(12)
    val usr = User(uuid, name, Set.empty[Account])
    (udmock.readUser _).expects(Some(List(uuid)), None).returns(List.empty[User])
    val t1 = userManager.deleteUser(uuid)
    assert(!t1)
    (udmock.readUser _).expects(Some(List(uuid)), None).returns(List(usr))
    (tdmock.readTransfer _).expects(None, Some(List.empty[Account]), Some(List.empty[Account]), None, None, None).returns(List.empty[Transfer])
    (udmock.deleteUser _).expects(uuid).returns(true)
    val t2 = userManager.deleteUser(uuid)
    assert(t2)
    
  }

}