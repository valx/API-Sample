package datatest

import akka.actor.Actor
import com.datastax.driver.core._
import scala.collection.JavaConversions._
import akka.event.Logging
import datatest.Utils._

/**
 * Akka actor which updates the Database (on write requests)
 */
class Updater extends Actor {

  val log = Logging(context.system, this)
  var cluster: Cluster = null
  private var session: Session = null

  // connect to Cassandra only once
  connect(Utils.cassandra_host)

  // receive WRITE request: ti represents the initial timestamp,  ty is the event type
  def receive = {

    case (ti:String,ty:String) => log.info("received message: "+ti+","+ty)
      if(ti.toLong>9999999999L){
        log.error("The timestamp should be expressed in seconds, maximum 10 digits")
      }
      else {
        val statement: PreparedStatement = session.prepare(
          "UPDATE "+Utils.cassandra_keyspace+"."+Utils.cassandra_table+" SET number = number + 1 WHERE minute=? AND type=?;"
        );
        val boundStatement: BoundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(new Integer(ti.toMinute), ty));
      }

    case _ => log.info("Unknown message")
  }

  def connect(node: String) {

    cluster = Cluster.builder().addContactPoint(node).build()
    val metadata = cluster.getMetadata()
    log.info("Connected to cluster: %s\n", metadata.getClusterName())
    metadata.getAllHosts() map {
      case host =>
        log.info("Datatacenter: %s; Host: %s; Rack: %s\n", host.getDatacenter(), host.getAddress(), host.getRack())
    }

    session = cluster.connect(Utils.cassandra_keyspace);

  }

  def close() {
    cluster.close()
  }

}
