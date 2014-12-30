package datatest

import akka.actor.Actor
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.routing._
import spray.http._
import MediaTypes._

/*
  Akka-Spray actor which receives http/REST requests and forwards them to other actors
 */
class Receiver extends Actor with HttpService{

  val log = Logging(context.system, this)

  def actorRefFactory = context

  def receive = runRoute(myRoute)

  val routerWrite = context.actorSelection("../routerWrite")

  val routerRead = context.actorSelection("../routerRead")

  // actions: write or read
  val myRoute =
  // WRITE request: ti represents the initial timestamp,  ty is the event type
    (path("write") & get) {
      parameters('ti, 'ty) { (ti, ty) =>
        val m = (ti,ty)
        routerWrite ! m
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          log.info("writing"+ti+"-"+ty)
          complete {
            <html>
              <body>
                <p>write message sent</p>
              </body>
            </html>
          }
        }
      }
    } ~
      // READ request: t1 represents the initial time, t2 the final time, ty is the type
      (path("read") & get) {
        parameters('t1, 't2, 'ty) { (t1,t2,ty) =>
          val m = (t1,t2,ty)
          implicit val timeout = Timeout(5 seconds)
          val reply = routerRead ? m
          val result = Await.result(reply, timeout.duration).asInstanceOf[String]
          respondWithMediaType(`application/json`) { // XML is marshalled to `text/xml` by default, so we simply override here
            complete {
              result
            }
          }
        }
      } ~
      path("") {
        get {
          respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
            complete {
              <html>
                <body>
                  <p>write? ti=timestamp1 ty=type</p>
                  <p>read? t1=timestamp1 t2=timestamp2 ty=type</p>
                </body>
              </html>
            }
          }
        }
      }
}
