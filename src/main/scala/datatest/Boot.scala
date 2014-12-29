package datatest

import akka.actor.{AddressFromURIString, ActorRef, ActorSystem, Props}
import akka.io.IO
import akka.routing.RoundRobinRouter
import com.typesafe.config.ConfigFactory
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object Boot extends App {

  // we need an ActorSystem to host our application in

  val config = ConfigFactory.load()

  implicit val systemMain = ActorSystem("datatest-MainSys",config.getConfig("akkaMain").withFallback(config))

  implicit val systemWorker = ActorSystem("datatest-WorkSys",config.getConfig("akkaWorker").withFallback(config))

  // create and start our service actor
  val updaterActor1 = systemMain.actorOf(Props[Updater], "updaterM1")
  val updaterActor2 = systemMain.actorOf(Props[Updater], "updaterM2")
  val updaterActor3 = systemWorker.actorOf(Props[Updater], "updaterW3")
  val updaterActor3String = "akka.tcp://datatest-WorkSys@127.0.0.1:2553/user/updaterW3"
  //val updaterActor4 = systemWorker.actorOf(Props[Updater], "updaterW4")
  val updaterActor4String = "akka.tcp://datatest-WorkSys@127.0.0.1:2553/user/updaterW4"

  println(updaterActor1.path  +"  " +  updaterActor3.path)
  //println(updaterActor1  +"  " +  updaterActor3)
 /* val routees = Vector[ActorRef](updaterActor1, updaterActor2, updaterActor3, updaterActor4)
  //Actor[akka://datatest-MainSys/user/updaterM1#-2003897832]  Actor[akka://datatest-WorkSys/user/updaterW3#-255480446
  // funziona: akka://datatest-MainSys/user/updaterM2
  NON funziona:
  */


  val routees = Vector[String]("/user/updaterM1","akka.tcp://datatest-WorkSys@127.0.0.1:2553/user/updaterW3")

  var remote = systemMain.actorSelection("akka://datatest-MainSys/user/updaterM2")
  remote ! "t1"

  remote = systemMain.actorSelection("akka.tcp://datatest-WorkSys@127.0.0.1:2553/user/updaterW3")
  remote ! "t21"

  val remote2 = systemMain.actorSelection("akka.tcp://datatest-WorkSys@127.0.0.1:2553/user/updaterW3")
  remote2 ! "t22"

  updaterActor3 ! "t3"








  val routerWrite = systemMain.actorOf(Props.empty.withRouter(
    RoundRobinRouter(routees = routees)),"routerWrite")

  val receiverActor = systemMain.actorOf(Props[ReceiverActor], "receiver-service")

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http)(systemMain) ? Http.Bind(receiverActor, interface = "localhost", port = 8080)

}

