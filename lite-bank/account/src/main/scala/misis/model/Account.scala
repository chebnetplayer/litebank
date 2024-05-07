package misis.model
case class Account(id: Int, amount: Int, category: Int) {
    def update(value: Int) = this.copy(amount = amount + value)
}

trait Command

// For account commands
case class AccountCreate(amount: Int, category: Int) extends Command
case class BankAccountCreate(amount: Int) extends Command
case class AccountUpdate(accountId: Int, value: Int, feeValue: Int = 0, nextAccountId: Option[Int] = None, previousAccountId: Option[Int] = None)
case class ShowAccountBalance(accountId: Int) extends Command

// For transfer commands
case class TransferStart(sourceId: Int, destinationId: Int, value: Int) extends Command
case class AccountFromAck(sourceId: Int, destinationId: Int, value: Int) extends Command
case class TransferCheckDestination(sourceId: Int, destinationId: Int, value: Int) extends Command
case class AccountToAck(sourceAccountId: Int, destinationAccountId: Int, value: Int) extends Command
trait Event
case class AccountCreated(accountId: Int, amount: Int) extends Event
case class AccountUpdated(
    accountId: Int,
    value: Int,
    feeValue: Int = 0,
    nextAccountId: Option[Int] = None,
    previousAccountId: Option[Int] = None,
    category: Option[Int] = None,
) extends Event
