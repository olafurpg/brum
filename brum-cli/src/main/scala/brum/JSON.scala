package brum

import java.{util => ju}

object JSON {
  case class Object(values: ju.LinkedHashMap[String, JSON]) extends JSON
  case class Array(values: ju.ArrayDeque[JSON]) extends JSON
  case class String(value: java.lang.String) extends JSON
}
sealed abstract class JSON {
  def renderAsString: String = {
    val out = new StringBuilder
    def loop(x: JSON): Unit = x match {
      case JSON.Object(values) =>
        out.addOne('{')
        val it = values.values().iterator()
        while (it.hasNext()) {
          loop(it.next())
          if (it.hasNext()) {
            out.addOne(',')
          }
        }
        out.addOne('}')
      case JSON.Array(values) =>
        out.addOne('[')
        val it = values.iterator()
        while (it.hasNext()) {
          loop(it.next())
          if (it.hasNext()) {
            out.addOne(',')
          }
        }
        out.addOne(']')
      case JSON.String(value) =>
        out
          .addOne('"')
          .addAll(escape(value))
          .addOne('"')
    }
    loop(this)
    out.toString()
  }
  private def escape(str: java.lang.String): String =
    str.flatMap {
      case '\\'  => "\\\\"
      case '\n'  => "\\n"
      case '"'   => "\""
      case other => other.toString
    }
}
