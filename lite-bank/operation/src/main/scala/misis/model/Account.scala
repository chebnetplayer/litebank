package misis.model

import java.util.UUID

case class Account(id: Int, amount: Int) {
    def update(value: Int) = this.copy(amount = amount + value)
}

trait Command
case class ShowAccountBalance(accountId: Int) extends Command
case class AccountCreate(initialAmount: Int, category: Int) extends Command
case class BankAccountCreate(initialAmount: Int) extends Command
case class AccountUpdate(
                          accountId: Int,
                          value: Int,
                          feeValue: Int = 0,
                          nextAccountId: Option[Int] = None,
                          previousAccountId: Option[Int] = None
                        ) extends Command
case class ReturnCashback(accountId: Int) extends Command

trait Event
case class AccountUpdated(
                           accountId: Int,
                           value: Int,
                           feeValue: Int = 0,
                           nextAccountId: Option[Int] = None,
                           previousAccountId: Option[Int] = None,
                           category: Option[Int] = None
                         ) extends Event