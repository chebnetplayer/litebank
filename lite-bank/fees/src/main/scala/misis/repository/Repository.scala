package misis.repository

import misis.model._
import scala.concurrent.Future
import scala.collection.mutable

class Repository(defaultFeeLimit: Int) {
  val defaultLimit = defaultFeeLimit
  private val accountsFeeLimits = mutable.Map.empty[Int, FeeLimit]

  def isAccountFeeLimitOpen(accountId: Int): Boolean = {
    accountsFeeLimits.contains(accountId)
  }

  def getFeeLimit(accountId: Int): Option[FeeLimit] = {
    accountsFeeLimits.get(accountId)
  }

  def createFeeLimit(accountId: Int, limit: Int = defaultFeeLimit): Future[FeeLimit] = {
    val accountLimit = FeeLimit(accountId, limit)
    accountsFeeLimits += (accountId -> accountLimit)
    Future.successful(accountLimit)
  }

  def isFeeLimitOver(accountId: Int, value: Int): Boolean = {
    accountsFeeLimits.get(accountId).exists(_.feeLimit - value <= 0)
  }

  def updateFeeLimit(accountId: Int, value: Int): Future[Unit] = {
    accountsFeeLimits.get(accountId) match {
      case Some(accountLimit) =>
        val updatedAccountLimit = accountLimit.updateFeeLimit(value)
        accountsFeeLimits += (accountId -> updatedAccountLimit)
        Future.successful(())
      case None =>
        Future.failed(new IllegalArgumentException(s"Limit with account $accountId does not exist"))
    }
  }


}