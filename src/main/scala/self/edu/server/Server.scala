package self.edu.server

import akka.actor.{Props, ActorSystem}
import self.edu.server.http.RequestDispatcher
import akka.io.IO
import spray.can.Http
import com.typesafe.config.ConfigFactory



/**
 * Created by alexey on 5/5/14.
 */
object Server {
  object Names {
    val Database = "Database"
  }
  val config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("server")
    system.actorOf(Props[Database], Names.Database)
    val dispatcher = system.actorOf(Props[RequestDispatcher], "RequestDispatcher")
    IO(Http).tell(Http.Bind(
      dispatcher,
      config.getString("server.http.host"),
      config.getInt("server.http.port")
    ), dispatcher)
  }
}
