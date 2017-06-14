package test.revolut.json

import java.time.LocalDateTime

class FindTransfersRequest (var uuids: List[String], var fromUuids: List[String],var toUuids: List[String],var amounts: List[(BigDecimal, BigDecimal)],var times: List[(LocalDateTime, LocalDateTime)],var rollbackStatuses: List[Boolean]){
  def this(){this(null, null, null, null, null, null)}
}