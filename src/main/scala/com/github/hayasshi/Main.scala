package com.github.hayasshi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.http.scaladsl.settings.ServerSettings
import javax.net.ssl.TrustManagerFactory
import java.security.KeyStore
import java.io.InputStream
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import java.security.SecureRandom
import akka.http.scaladsl.HttpsConnectionContext
import akka.http.scaladsl.ConnectionContext

object Main extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val systemEc: ExecutionContext = system.dispatcher

  val bindingHttp = 
    Http()
      .newServerAt("0.0.0.0", 80)
      .bindFlow(new Proxy(system).flow)
      .map(_.addToCoordinatedShutdown(30.seconds))

  val password: Array[Char] = "change me".toCharArray // do not store passwords in code, read them from somewhere safe!

  val ks: KeyStore = KeyStore.getInstance("PKCS12")
  val keystore: InputStream = getClass.getClassLoader.getResourceAsStream("server.p12")

  require(keystore != null, "Keystore required!")
  ks.load(keystore, password)

  val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
  keyManagerFactory.init(ks, password)

  val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
  tmf.init(ks)

  val sslContext: SSLContext = SSLContext.getInstance("TLS")
  sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
  val https: HttpsConnectionContext = ConnectionContext.httpsServer(sslContext)
  val bindingHttps = 
    Http()
      .newServerAt("0.0.0.0", 443).enableHttps(https)
      .bindFlow(new Proxy(system).flow)
      .map(_.addToCoordinatedShutdown(30.seconds))
  
  sys.addShutdownHook {
    Await.result(system.terminate(), 30.seconds)
  }
  
}
