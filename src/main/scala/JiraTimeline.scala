import java.io.PrintWriter
import java.time._
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import model.{Activity, DataSet, Entry}

import scala.annotation.tailrec
import scala.io.Source
import scala.xml.XML
import scalaj.http.Http

object JiraTimeline extends App {

  val template = Source.fromResource("template.html").getLines().mkString("\n")

  val now = Instant.now()
  val epochTime = now.minus(35, ChronoUnit.DAYS).toEpochMilli

  val url = s"${Conf.jiraUrl}/activity?maxResults=200&streams=user+IS+${Conf.searchUser}&activity+IS+issue:transition&streams=update-date+AFTER+$epochTime&os_authType=basic"
  val response = Http(url).auth(Conf.username, Conf.password).timeout(1000, 10000).asString
  val activityXml = XML.loadString(response.body)

  val entries = Activity(activityXml, Conf.username).entries.filterNot(e => Conf.filterNot(e.categoryTerm)).reverse.toList
  val groups = entries.map(_.ticketUrl).distinct.filter(_.nonEmpty).map(toGroup)
  val events = entries.map(entry => DataSet.Point(entry.categoryTerm, entry.updated, group = Some(entry.ticket)))

  val intervals = entries.groupBy(_.obj.title) flatMap { case (ticket, entries) =>
    toIntervals(ticket, entries)
  }

  val formattedDate =  DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(now)
  val title = s"${Conf.searchUser} $formattedDate"

  val html = template
    .replaceAllLiterally("$title", title)
    .replaceAllLiterally("$items", (events ++ intervals) map (_.toJson) mkString ("[", ",\n", "]"))
    .replaceAllLiterally("$groups", groups map (_.toJson) mkString ("[", ",\n", "]"))
    .replaceAllLiterally("$startDate", now.minus(30, ChronoUnit.DAYS).toEpochMilli.toString)

  write(s"$title.html")(html)

  def toGroup(ticketUrl: String): DataSet = {
    val ticket = ticketUrl.split("/").last
    DataSet.Group(ticketUrl.split("/").last, s"""<a href="$ticketUrl">$ticket</a>""")
  }

  @tailrec
  def toIntervals(group: String, entries: List[Entry], startTime: Option[Instant] = None, acc: List[DataSet] = Nil): List[DataSet] = entries match {
    case Nil                                           => acc ++ startTime.map(s => DataSet.Background("", s, now, group = Some(group)))
    case head :: tail if Conf.start(head.categoryTerm) => toIntervals(group, tail, Some(head.updated), acc)
    case head :: tail if Conf.end(head.categoryTerm)   => toIntervals(group, tail, None, acc ++ startTime.map(s => DataSet.Background("", s, head.updated, group = Some(group))))
    case _ :: tail                                     => toIntervals(group, tail, startTime, acc)
  }

  def write(file: String)(data: String) = new PrintWriter(file) { write(data); close() }
}
