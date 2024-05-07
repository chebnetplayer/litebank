package misis.repository

import misis.model._
import scala.concurrent.Future
import scala.collection.mutable

class Repository() {
  private val accountsCashbacks = mutable.Map.empty[Int, AccountCashback]
  private val categoryCashbackPercent = mutable.Map.empty[Int, Int]

  categoryCashbackPercent += (1 -> 10)
  categoryCashbackPercent += (2 -> 20)
  categoryCashbackPercent += (3 -> 30)

  def isCashbackOpen(accountId: Int): Boolean = {
    accountsCashbacks.contains(accountId)
  }

  def createCashback(accountId: Int, cashback: Int = 0): Future[AccountCashback] = {
    val accountCashback = AccountCashback(accountId, cashback)
    accountsCashbacks += (accountId -> accountCashback)
    Future.successful(accountCashback)
  }

  def updateCashback(accountId: Int, value: Int): Future[Unit] = {
    accountsCashbacks.get(accountId) match {
      case Some(accountsCashback) =>
        val updatedCashback = accountsCashback.updateCashback(value)
        accountsCashbacks += (accountId -> updatedCashback)
        Future.successful(())
      case None =>
        Future.failed(new IllegalArgumentException(s"Limit with account $accountId does not exist"))
    }
  }

  def resetCashback(accountId: Int): Future[Unit] = {
    accountsCashbacks.get(accountId) match {
      case Some(accountsCashback) =>
        val updatedCashback = accountsCashback.resetCashback()
        accountsCashbacks += (accountId -> updatedCashback)
        Future.successful(())
      case None =>
        Future.failed(new IllegalArgumentException(s"Limit with account $accountId does not exist"))
    }
  }

  def getCashback(accountId: Int): Future[Option[AccountCashback]] = {
    Future.successful(accountsCashbacks.get(accountId))
  }

  def calculateCashbackAmount(category: Int, amount: Int): Option[Int] = {
    categoryCashbackPercent.get(category) match {
      case Some(cashbackPercent) =>
        val cashback = (amount * cashbackPercent) / 100
        Some(cashback)
      case None => None
    }
  }
}
