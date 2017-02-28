package model

import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

import scala.util.Try

/**
  * Created by ki4ki on 02.01.2017.
  */
object DataSet {

  private val idGen = new AtomicInteger(0)
  def getId: Int = idGen.incrementAndGet()

  def Box(content: String, date: Instant, title: Option[String] = None, group: Option[String] = None) = DataSet(
    content = content,
    start = Some(date),
    title = title,
    group = group
  )

  def Point(content: String, date: Instant, title: Option[String] = None, group: Option[String] = None): DataSet =
    Box(content, date, title, group) copy (typ = Some("point"))

  def Range(content: String, start: Instant, end: Instant, title: Option[String] = None, group: Option[String] = None) = DataSet(
    content = content,
    start = Some(start),
    end = Some(end),
    title = title,
    group = group
  )

  def Background(content: String, start: Instant, end: Instant, group: Option[String] = None) = DataSet(
    content = content,
    start = Some(start),
    end = Some(end),
    group = group,
    typ = Some("background")
  )

  def Group(id: String, content: String) = DataSet(
    id = id,
    content = content
  )

}

case class DataSet(
  id     : String = DataSet.getId.toString,
  content: String,
  start  : Option[Instant] = None,
  end    : Option[Instant] = None,
  title  : Option[String]  = None,
  group  : Option[String]  = None,
  typ    : Option[String]  = None
) {
  private def optField(key: String)(value: Option[String]): Option[(String, String)] = value map (key -> _)
  private def normalize(str: String): String = str.replaceAllLiterally("\"", "\\\"")
  private def stringify(str: String): String = Try(str.toInt).fold(_ => s"`$str`", _ => str)

  def toMap: Map[String, String] = Map(
    "id" -> id,
    "content" -> normalize(content)
  ) ++ List(
    optField("start")(start map (_.toString)),
    optField("end")(end map (_.toString)),
    optField("title")(title map normalize),
    optField("group")(group),
    optField("type")(typ)
  ).flatten

  def toJson = toMap map { case (key, value) => s""""$key": ${stringify(value)}""" } mkString ("{", ", ", "}")
}
