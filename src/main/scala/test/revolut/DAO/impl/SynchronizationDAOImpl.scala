package test.revolut.DAO.impl

import test.revolut.data.storage.InMemory._
import test.revolut.DAO.SynchronizationDAO

class SynchronizationDAOImpl extends SynchronizationDAO {
  private def getDataByUuid(uuid: String): Option[Data] = {
    val searchResult: List[Data] = storage.accounts.filter { x => x.uuid.equals(uuid) }.toList :::
      storage.currencies.filter { x => x.uuid.equals(uuid) }.toList :::
      storage.transfers.filter { x => x.uuid.equals(uuid) }.toList :::
      storage.users.filter { x => x.uuid.equals(uuid) }.toList

    if (searchResult.size == 0) {
      None
    } else {
      Some(searchResult.last)
    }
  }

  override def getSyncObjectByUuids(uuids: List[String]): List[Object] = {
    uuids.sortBy(x => x).map { x => getDataByUuid(x).getOrElse(new Object) }
  }

  override def syncOn(on: List[Object], action: () => Unit) {
    if (!on.isEmpty) {
      if (on.size == 1) {
        on.last.synchronized({
          action()
        })
      } else {
        on match {
          case first :: list => { first.synchronized({ syncOn(list, action) }) }
        }
      }
    }
  }
  
  
}