package datatest

/**
 * implicit covert timestamp String into an Int representing the minutes from the epoch...
 */
object Utils {

  class TimestampMinute(val timestamp: String) {
    def toMinute: Int = (timestamp.toLong / 60).toInt
  }

  implicit def intToMin(i: String) = new TimestampMinute(i)

  def cassandra_keyspace = "datatest"
  def cassandra_table = "events"
  def cassandra_host = "127.0.0.1"
  def akka_workerSys = "127.0.0.1:2553"
  def akka_mainSys = "127.0.0.1:2552"
  def spray_HttpHost = "127.0.0.1"
  def spray_HTTPPort = 8080
}