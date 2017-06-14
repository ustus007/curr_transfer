package test.revolut.DAO

import scala.collection.mutable.HashMap
import test.revolut.DAO.AbstractDAO

object DAOStorage {

  val named: HashMap[String, AbstractDAO] = new HashMap[String, AbstractDAO];

  def getOrCreate(name: String, action: () => AbstractDAO): AbstractDAO = {
    named.synchronized({
      if (!named.keySet.contains(name)) {
        named.put(name, action())
      }
      named(name)
    })
  }

}