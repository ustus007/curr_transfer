package test.revolut.utils

import scala.util.Random
import test.revolut.data.storage.InMemory._

object TestUtils {
  
  def genString(length:Int):String={
    var l = length
    if (l < 1) {l = 1}
    val x = Random.alphanumeric
    val result = new StringBuilder().append(x take l).toString()
    result
  }
  
  def restoreStorage(){
    storage = Storage(new PSet[User], new PSet[Account], new PSet[Currency], new PList[Transfer])
  }
}