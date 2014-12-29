package datatest

import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json._

class Receiver extends Actor with HttpService{

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)

  val routerWrite = context.actorSelection("../routerWrite")

  val routerRead = context.actorSelection("../routerRead")

  val myRoute =

    (path("write") & get) {
      parameters('ti, 'ty) { (ti, ty) =>
        val m = (ti,ty)
        routerWrite ! m
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          println("wrote!"+ti+"-"+ty)
          complete {
            <html>
              <body>
                <p>writing</p>
              </body>
            </html>
          }
        }
      }
    } ~
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
          //println("read1!" + t1)
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
