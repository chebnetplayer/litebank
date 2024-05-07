package misis.model

case class FeeLimit(accountId: Int, feeLimit: Int) {
  def updateFeeLimit(value: Int) = this.copy(feeLimit = feeLimit - value)
}

trait Command
case class AccountUpdate(
                          accountId: Int,
                          value: Int,
                          feeValue: Int = 0,
                          nextAccountId: Option[Int] = None
                        ) extends Command
case class TransferToAccount(sourceId: Int, destinationId: Int, value: Int) extends Command

trait Event
case class AccountUpdated(
                           accountId: Int,
                           value: Int,
                           feeValue: Int = 0,
                           nextAccountId: Option[Int] = None,
                           previousAccountId: Option[Int] = None,
                           category: Option[Int] = None
                         ) extends Event