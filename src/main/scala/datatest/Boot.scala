package datatest

import java.io.FileInputStream

import akka.actor.{AddressFromURIString, ActorRef, ActorSystem, Props}
import akka.io.IO
import akka.routing.RoundRobinRouter
import com.typesafe.config.ConfigFactory
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

/*
 * Create and initialize the Akka Actor systems: MainSystem and WorkersSystem
 */
object Boot extends App {

  // load configuration from src/main/resources/application.conf
  val config = ConfigFactory.load()

  // main Akka actor system, including Http Receiver, Router balance , 2 Update workers
  implicit val systemMain = ActorSystem("datatest-MainSys",config.getConfig("akkaMain").withFallback(config))

  // workers  Akka actor system, including 2 Update workers
  implicit val systemWorker = ActorSystem("datatest-WorkSys",config.getConfig("akkaWorker").withFallback(config))

  // create and start our WRITE path workers actors for both systems
  val updaterActor1 = systemMain.actorOf(Props[Updater], "updaterM1")
  val updaterActor2 = systemMain.actorOf(Props[Updater], "updaterM2")
  val updaterActor3 = systemWorker.actorOf(Props[Updater], "updaterW3")
  val updaterActor3String = "akka.tcp://datatest-WorkSys@"+Utils.akka_workerSys+"/user/updaterW3"
  val updaterActor4 = systemWorker.actorOf(Props[Updater], "updaterW4")
  val updaterActor4String = "akka.tcp://datatest-WorkSys@"+Utils.akka_workerSys+"/user/updaterW4"

  // create the Router (load balance) and connect the 4 workers to it
  val routeesWrite = Vector[String]("/user/updaterM1",updaterActor3String,"/user/updaterM2",updaterActor4String)
  val routerWrite = systemMain.actorOf(Props.empty.withRouter(
    RoundRobinRouter(routees = routeesWrite)),"routerWrite")

  // create workers for READ path
  val readerActor1 = systemMain.actorOf(Props[Reader], "readerM1")
  val readerActor2 = systemWorker.actorOf(Props[Reader], "readerW2")
  val readerActor2String = "akka.tcp://datatest-WorkSys@"+Utils.akka_workerSys+"/user/readerW2"
  val routeesRead = Vector[String]("/user/readerM1",readerActor2String)

  // create readers router
  val routerRead = systemMain.actorOf(Props.empty.withRouter(
    RoundRobinRouter(routees = routeesRead)),"routerRead")

  // create HTTP/Rest Spray-Akka Actor
  val receiverActor = systemMain.actorOf(Props[Receiver], "receiver-service")
  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server with our service actor as the handler
  IO(Http)(systemMain) ? Http.Bind(receiverActor, interface = Utils.spray_HttpHost, port = Utils.spray_HTTPPort)

}

