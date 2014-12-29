package datatest

/**
 * Created by valerio on 12/29/14.
 */
object Utils {

  class TimestampMinute(val timestamp: String) {
    def toMinute: Int = (timestamp.toLong / 60).toInt
  }

  implicit def intToMin(i: String) = new TimestampMinute(i)

}