package proxy7s.akka.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import org.scalatest.funspec.AnyFunSpec
import java.util.concurrent.atomic.AtomicReference
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.duration._
import org.scalatest.diagrams.Diagrams
import akka.http.scaladsl.unmarshalling.Unmarshaller
import scala.concurrent.ExecutionContext
import org.scalatest.BeforeAndAfterAll
import scala.concurrent.Await

class ProxyTest extends AnyFunSpec with Diagrams with ScalaFutures with BeforeAndAfterAll {

  override def spanScaleFactor = 10.0d

  implicit val system: ActorSystem  = ActorSystem()
  implicit val ec: ExecutionContext = system.dispatcher

  val holder = new AtomicReference[HttpRequest]()
  val testRoute = extractRequestContext { ctx =>
    holder.set(ctx.request)
    complete("OK")
  }
  Http().newServerAt("0.0.0.0", 8080).bind(testRoute).foreach(_.addToCoordinatedShutdown(10.seconds))

  override protected def afterAll(): Unit = {
    Await.ready(system.terminate(), 10.seconds)
    super.afterAll()
  }

  describe("Proxy.flow") {

    val proxyFlow = new Proxy(system).flow

    it("リクエスト内容を 127.0.0.1:8080 にルーティングする") {
      val request = HttpRequest(
        method = HttpMethods.POST,
        uri = Uri("http://127.0.0.1:8081/path1?key2=value3"),
        entity = HttpEntity("test body"),
        headers = Seq(RawHeader("test-header", "header-value"))
      )
      val response     = Source.single(request).via(proxyFlow).runWith(Sink.head).futureValue
      val responseBody = response.entity.dataBytes.runReduce(_ ++ _).map(_.decodeString("UTF-8")).futureValue
      assert(responseBody == "OK")

      val actualRequest = holder.get()
      assert(actualRequest.method == request.method)
      assert(actualRequest.uri == request.uri.withAuthority("127.0.0.1", 8080))
      assert(actualRequest.entity == request.entity)
      assert(actualRequest.headers.contains(RawHeader("test-header", "header-value")))
    }

  }

}
