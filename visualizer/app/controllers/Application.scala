package controllers

import java.net.{DatagramPacket, DatagramSocket}

import scala.concurrent.Future

import play.api.http.MimeTypes
import play.api.libs.EventSource
import play.api.libs.iteratee.{Enumeratee, Enumerator}
import play.api.mvc._

object Application extends Controller {

  //TODO: Enable Multicasting so we can have many clients
  val serverSocket = new DatagramSocket(9003)
  def index = Action {
    Ok(views.html.index())
  }

  def events = Action {
    Ok.chunked(userEvents().through(EventSource())).as(MimeTypes.EVENT_STREAM)
  }

  private def userEvents(): Enumerator[String] = {
    import scala.concurrent.ExecutionContext.Implicits._
    //TODO: Use akka.io for udp. This is blocking right now
    Enumerator.repeatM(
      Future {
        val receiveData = new Array[Byte](2048);
        val receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        new String(receivePacket.getData, 0, receivePacket.getLength)
    }).through(Enumeratee.filter(s => s.contains("user")))
  }

}