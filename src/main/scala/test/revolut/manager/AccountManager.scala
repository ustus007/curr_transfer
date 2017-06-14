package test.revolut.manager

import test.revolut.entity.User
import test.revolut.entity.Account
import test.revolut.entity.Currency

trait AccountManager extends AbstractManager {

  def createAccount(currency: Currency, owner: User): Account
  def findAccount(uuid: Option[String], amounts: Option[(BigDecimal, BigDecimal)], currency: Option[Currency], owner: Option[User]): List[Account]
  def topUpAccountFromOutside(uuid: String, additionalSum: BigDecimal): Option[Account]
  def withdrawFromAccountOutside(uuid: String, takenSum: BigDecimal): Option[Account]
  def deleteAccount(uuid: String): Boolean
}