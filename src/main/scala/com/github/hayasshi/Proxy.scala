package com.github.hayasshi

import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.Http
import akka.actor.ActorSystem
import akka.NotUsed
import akka.http.scaladsl.model.HttpResponse

class Proxy(system: ActorSystem) {
  val connectionPool = Http(system).superPool[NotUsed]()
  
  val flow: Flow[HttpRequest, HttpResponse, NotUsed] = 
    Flow[HttpRequest]
      .map(req => (req, NotUsed))
      .via(connectionPool)
      .map(_._1.get)
}
