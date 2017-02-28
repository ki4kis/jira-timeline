import com.typesafe.config.ConfigFactory
import com.github.andr83.scalaconfig._

object Conf {
  System.setProperty("config.file", "application.conf")

  private val conf = ConfigFactory.load()

  val jiraUrl: String = conf.as[String]("url")
  val username: String = conf.as[String]("username")
  val password: String = conf.as[String]("password")

  val searchUser: String = conf.as[Option[String]]("searchUser") getOrElse username

  val start: Set[String] = conf.as[Option[Set[String]]]("start") getOrElse Set.empty
  val end: Set[String] = conf.as[Option[Set[String]]]("end") getOrElse Set.empty
  val filterNot: Set[String] = conf.as[Option[Set[String]]]("hide") getOrElse Set.empty
}
