package misis.model

case class TransferStart(sourceId: Int, destinationId: Int, value: Int)
case class AccountFromAck(sourceId: Int, destinationId: Int, value: Int) extends Command
case class TransferCheckDestination(sourceId: Int, destinationId: Int, value: Int) extends Command
case class AccountToAck(sourceId: Int, destinationId: Int, value: Int) extends Command
