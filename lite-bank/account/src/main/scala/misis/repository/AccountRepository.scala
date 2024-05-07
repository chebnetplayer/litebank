package misis.repository

import misis.model.Account
import scala.concurrent.Future
import scala.collection.mutable

class AccountRepository(){

    private var accountId = 0
    private var bankAccountId = 0;
    private def createId(): Int = {
        accountId += 1
        accountId
    }

    private  val accounts = mutable.Map.empty[Int, Account]

    def getAccountKeys(): Iterable[String] = {
        accounts.keys.map(_.toString)
    }

    def getAccount(accountId: Int): Future[Option[Account]] = {
        Future.successful(accounts.get(accountId))
    }

    def getAccountCategory(accountId: Int): Option[Int] = {
        accounts.get(accountId).map(_.category)
    }

    def isAccountOpen(accountId: Int): Boolean = {
        accounts.contains(accountId)
    }

    def createAccount(amount: Int = 0, category: Int): Future[Account] = {
        val newId = createId()
        val account = Account(newId, amount, category)
        accounts += (newId -> account)
        Future.successful(account)
    }

    def createBankAccount(amount: Int = 0): Future[Account] = {
        val bankAccount = Account(bankAccountId, amount, 0)
        accounts += (bankAccountId -> bankAccount)
        Future.successful(bankAccount)
    }
    def updateAccount(accountId: Int, value: Int): Future[Unit] = {
        accounts.get(accountId) match {
            case Some(account) =>
                if (accounts.get(accountId).exists(_.amount + value >= 0)) {
                val updatedAccount = account.update(value)
                accounts += (accountId -> updatedAccount)
                Future.successful()
            } else {
            Future.failed(new IllegalArgumentException(s"This account does not have enough money"))
            }
            case None =>
                Future.failed(new IllegalArgumentException(s"Account does not exist"))
        }
    }
}
