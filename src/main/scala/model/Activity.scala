package model

import java.time.Instant

object Activity {

  def apply(node: xml.Node, user: String): Activity = parseActivity(node, user)

  private def parseDate(str: String): Instant = Instant.parse(str)
  private def StrOpt(str: String): Option[String] = str.trim match {
    case ""  => None
    case str => Some(str)
  }

  def parseActivity(node: xml.Node, user: String): Activity = Activity(node \\ "entry" map parseEntry collect {
    case e if e.categoryTerm contains "Comment" => e.copy(
      categoryTerm = s"""<a href="${e.obj.link}">Comment</a>"""
    )
    case e if e.categoryTerm.nonEmpty           => e
  })

  def parseEntry(node: xml.Node): Entry = Entry(
    title = (node \ "title").text,
    content = StrOpt((node \ "content").text),
    author = parseAuthor((node \ "author").head),
    published = parseDate((node \ "published").text),
    updated = parseDate((node \ "updated").text),
    categoryTerm = (node \ "category" \@ "term").capitalize,
    obj = parseActivityObject((node \ "object").head),
    target = (node \\ "target").headOption map parseActivityTarget
  )

  def parseAuthor(node: xml.Node): Author = Author(
    name = (node \ "name").text,
    email = (node \ "email").text,
    uri = (node \ "uri").text,
    username = (node \ "username").text
  )

  def parseActivityObject(node: xml.Node): ActivityObject = ActivityObject(
    title = (node \ "title").text,
    summary = (node \ "summary").text,
    link = node \ "link" \@ "href",
    typ = (node \ "type").text
  )

  def parseActivityTarget(node: xml.Node): ActivityTarget = ActivityTarget(
    title = (node \ "title").text,
    summary = (node \ "summary").text,
    link = node \ "link" \@ "href"
  )
}

case class Activity(
  entries: Seq[Entry]
)

case class Entry(
  title: String,
  content: Option[String],
  author: Author,
  published: Instant, // 2017-01-01T23:56:53.967Z
  updated: Instant,
  categoryTerm: String,
  obj: ActivityObject,
  target: Option[ActivityTarget]
) {
  def ticket = target map (_.title) getOrElse obj.title
  def ticketUrl = target map (_.link) getOrElse obj.link
}

case class Author(
  name: String,
  email: String,
  uri: String,
  username: String
)

case class ActivityObject(
  title: String,
  summary: String,
  link: String,
  typ: String
)

case class ActivityTarget(
  title: String,
  summary: String,
  link: String
)
