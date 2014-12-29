package datatest

import akka.actor.Actor
import com.datastax.driver.core._
import scala.collection.JavaConversions._
import akka.event.Logging

/**
 * Created by valerio on 12/28/14.
 */

class Updater extends Actor {
  val log = Logging(context.system, this)
  var cluster: Cluster = null
  private var session: Session = null

  connect("127.0.0.1")

  def receive = {
    case m:String => log.info("received message: "+m)
  }

  def connect(node: String) {

    cluster = Cluster.builder().addContactPoint(node).build()
    val metadata = cluster.getMetadata()
    printf("Connected to cluster: %s\n",
      metadata.getClusterName())
    metadata.getAllHosts() map {
      case host =>
        printf("Datatacenter: %s; Host: %s; Rack: %s\n",
          host.getDatacenter(), host.getAddress(), host.getRack())
    }

    session = cluster.connect("datatest");
    val statement:PreparedStatement = session.prepare(
      "UPDATE datatest.events SET number = number + 5 WHERE minute=2 AND type='EventType2';"
    );

    val boundStatement:BoundStatement = new BoundStatement(statement);

    session.execute(boundStatement);

  }

  def close() {
    cluster.close()
  }

}
