package datatest

import akka.actor.Actor
import akka.event.Logging
import com.datastax.driver.core._
import scala.collection.JavaConversions._
import spray.json._
import DefaultJsonProtocol._
import datatest.Utils._


/**
 * Created by valerio on 12/28/14.
 */

case class Record(tipe: String, minute:Int, number:Long)

class Reader extends Actor {

  val log = Logging(context.system, this)
  var cluster: Cluster = null
  private var session: Session = null

  connect("127.0.0.1")

  def receive = {

    case (t1:String,t2:String,ty:String) =>
      if(t1.toLong>9999999999L | t2.toLong>9999999999L){
        sender() !
          Map[String,String]("error"->"The timestamp should be expressed in seconds, maximum 10 digits").toJson.prettyPrint
      }
      else {
        log.info("received message: " + t1 + "," + t2 + "," + ty + "- Range:" + t1.toMinute + "-" + t2.toMinute)
        val statement: PreparedStatement = session.prepare(
          "SELECT * from datatest.events WHERE minute>=? and minute<=? AND type IN (?) ;"
        );
        val boundStatement: BoundStatement = new BoundStatement(statement);
        val results: Iterable[Row] = session.execute(boundStatement.bind(new Integer(t1.toMinute), new Integer(t2.toMinute), ty));

        val resList: Iterable[Map[String, String]] = results.map {
          row =>
            Map[String, String](
              "type" -> row.getString("type"),
              "minute" -> row.getInt("minute").toString,
              "number" -> row.getLong("number").toString
            )
        }

        sender() ! resList.toJson.prettyPrint
      }
    case _ => log.info("Unknown message")
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

  }

  def close() {
    cluster.close()
  }

}
