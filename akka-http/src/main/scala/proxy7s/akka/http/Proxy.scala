package proxy7s.akka.http

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.Flow

class Proxy(system: ActorSystem) {

  val connectionPool = Http(system).superPool[NotUsed]()

  val flow: Flow[HttpRequest, HttpResponse, NotUsed] =
    Flow[HttpRequest]
      .map { req =>
        (req.withUri(req.uri.withAuthority("127.0.0.1", 8080)), NotUsed)
      }
      .via(connectionPool)
      .map(_._1.get)

}
