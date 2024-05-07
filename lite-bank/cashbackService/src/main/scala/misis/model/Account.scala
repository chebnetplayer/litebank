package misis.model

case class AccountCashback(accountId: Int, cashback: Int) {
    def updateCashback(value: Int) = this.copy(cashback = cashback + value)
    def resetCashback() = this.copy(cashback = 0)
}

trait Command
case class ShowAccountBalance(accountId: Int) extends Command

case class AccountCreate(amount: Int) extends Command

case class AccountUpdate(accountId: Int,
                         value: Int,
                         feeValue: Int = 0,
                         nextAccountId: Option[Int] = None,
                         previousAccountId: Option[Int] = None
                        ) extends Command

case class ReturnCashback(accountId: Int) extends Command

trait Event
case class AccountUpdated(accountId: Int, value: Int, feeValue: Int = 0, nextAccountId: Option[Int] = None, previousAccountId: Option[Int] = None, category: Option[Int] = None) extends Event
case class CashbackUpdated(
                            accountId: Int,
                            value: Int
                          )