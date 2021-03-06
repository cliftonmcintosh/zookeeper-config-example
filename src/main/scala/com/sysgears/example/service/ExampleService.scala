package com.sysgears.example.service

import java.util.Date

import akka.actor.{Actor, ActorLogging}
import com.sysgears.example.config.ZooKeeperConfiguration
import org.apache.curator.framework.CuratorFramework
import spray.can.Http
import spray.http._

/**
 * HTTP Service actor.
 */
class ExampleService(implicit val zkClient: CuratorFramework) extends Actor with ActorLogging with ZooKeeperConfiguration {

  override def preStart() {
    log.info("Example service is running in %s environment.".format(Environment))
  }

  override def postStop() {
    log.info("Example service has been terminated.")
  }

  def receive = {

    case "bind-parameters" => {
      val host = getSetting("%s.host".format(Service)).asString
      val port = getSetting("%s.port".format(Service)).asInt
      sender() !(host, port)
    }

    case _: Http.Connected => {
      sender() ! Http.Register(self)
    }

    case HttpRequest(HttpMethods.GET, Uri.Path("/example"), _, _, _) => {
      sender() ! HttpResponse(status = StatusCodes.OK,
        entity = HttpEntity(MediaTypes.`text/html`, getIndex))
    }

    case HttpRequest(HttpMethods.GET, Uri.Path("/ping"), _, _, _) => {
      sender() ! HttpResponse(status = StatusCodes.OK,
        entity = HttpEntity(MediaTypes.`text/html`, "<h2>pong!</h2>"))
    }

    case _: HttpRequest => {
      sender() ! HttpResponse(status = StatusCodes.NotFound,
        entity = HttpEntity(MediaTypes.`text/html`, "<h2>HTTP 404 - Not Found</h2>"))
    }

    case _ => {
      sender() ! HttpResponse(status = StatusCodes.InternalServerError,
        entity = HttpEntity(MediaTypes.`text/html`, "<h2>HTTP 500 - Internal Server Error</h2>"))
    }
  }

  private def getIndex: String =
    """
      |<html>
      | <head>
      |   <title>Index</title>
      | </head>
      | <body>
      |   <h1>Welcome to the <i>%s service</i></h1>
      |   <h3>Resources:</h3>
      |   <ul>
      |     <li><a href="/ping">/ping</a></li>
      |   </ul>
      |   <h3>Configuration:</h3>
      |   <table>
      |     <tr>
      |       <td>Environment</td><td><b>%s</b></td>
      |     </tr>
      |     <tr>
      |       <td>Startup Time</td><td>%s</td>
      |     </tr>
      |     <tr>
      |       <td>Host</td><td>%s</td>
      |     </tr>
      |     <tr>
      |       <td>Port</td><td>%s</td>
      |     </tr>
      |     <tr>
      |       <td>Database URL</td><td>%s</td>
      |     </tr>
      |     <tr>
      |       <td>Max. Number of Connections</td><td>%s</td>
      |     </tr>
      |   </table>
      | </body>
      |</html>
    """.stripMargin.format(Service, Environment, new Date(context.system.startTime),
        getSetting("%s.host".format(Service)).asString, getSetting("%s.port".format(Service)).asInt,
        getSetting("db.host").asString + getSetting("%s.db.name".format(Service)).asString + "?user=" +
          getSetting("%s.db.user".format(Service)).asString + "&password=" + getSetting("%s.db.password".format(Service)).asString,
        getSetting("db.maxConnections").asInt)
}