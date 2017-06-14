package test.revolut.data.storage

import collection.mutable.{ HashSet, SynchronizedSet, ArrayBuffer, SynchronizedBuffer }
import java.time.LocalDateTime
import java.io.FileOutputStream
import java.nio.file.Files
import java.io.ObjectOutputStream
import java.nio.file.Paths
import scala.util.Try
import java.io.FileInputStream
import java.io.ObjectInputStream
import scala.util.Failure
import scala.util.Success
import java.io.File


object InMemory {
  
  final val VERSION = 1L
  
  //thread protected mutable set
  class PSet[T] extends HashSet[T] with SynchronizedSet[T] {}
  
  class PList[T] extends collection.mutable.ArrayBuffer[T] with collection.mutable.SynchronizedBuffer[T] {}
  
  @SerialVersionUID(VERSION)
  abstract class Data extends Serializable
  
  @SerialVersionUID(VERSION)
  case class Currency(uuid:String, var shortName: String,var fullName: String) extends Data {
      def equals(that: Currency): Boolean = this.uuid.equals(that.uuid)
      def ==(that: Currency) = this equals that
  }
  
  @SerialVersionUID(VERSION)
  case class Account(uuid:String, currency: Currency,var amount: BigDecimal,var user: User) extends Data{
      def equals(that: Account): Boolean = this.uuid.equals(that.uuid)
      def ==(that: Account) = this equals that
  }
  
  @SerialVersionUID(VERSION)
  case class User(uuid:String, var fullName: String, accounts: PSet[Account]) extends Data{
      def equals(that: User): Boolean = this.uuid.equals(that.uuid)
      def ==(that: User) = this equals that
  }
  
  @SerialVersionUID(VERSION)
  case class Transfer(uuid:String, from: Account, to: Account, amount: BigDecimal, moment: LocalDateTime, var rolledBack: Boolean) extends Data{
      def equals(that: Transfer): Boolean = this.uuid.equals(that.uuid)
      def ==(that: Transfer) = this equals that
  }
  
  @SerialVersionUID(VERSION)
  case class Storage(users: PSet[User], accounts:PSet[Account], currencies:PSet[Currency], transfers: PList[Transfer]) extends Data {}
  
  var storage = Storage(new PSet[User], new PSet[Account], new PSet[Currency], new PList[Transfer])
  
  def load(file:File){
      if (file != null && Files.exists(Paths.get(file.getAbsolutePath))) {
        val ios = new ObjectInputStream(new FileInputStream(file.getAbsolutePath))
        Try(ios.readObject()) match {
          case Success(s) => {
            storage = s.asInstanceOf[Storage]
          }
          case Failure(e) => { }
        }
        ios.close
      }
  }
  
  def save(filename:String){
    var fileName = filename
    if (!(Files.exists(Paths.get(fileName)))) {
      if (!fileName.endsWith(".data")) {
        fileName = fileName + ".data"
      }
    }
    val oos = new ObjectOutputStream(new FileOutputStream(fileName))
    Try(oos.writeObject(storage.asInstanceOf[Storage])) match {
      case _ => {}
    }
    oos.close()
  }
}