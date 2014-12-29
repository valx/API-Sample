package datatest

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json._
import DefaultJsonProtocol._

class ReceiverActor extends Actor with HttpService{

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)

  val routerWrite = context.actorSelection("../routerWrite")

  val myRoute =

    (path("write") & get) {
      parameters('ti, 'ty) { (ti, ty) =>
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          val m = (ti,ty)
          routerWrite ! m
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
        parameters('t1, 't2) { (t1, t2) =>
          println("read!" + t1+"-"+t2)
          respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
            complete {
              <html>
                <body>
                  <p>reading</p>
                </body>
              </html>
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
                  <p>read? t1=timestamp1 t2=timestamp2</p>
                </body>
              </html>
            }
          }
        }
      }
}
