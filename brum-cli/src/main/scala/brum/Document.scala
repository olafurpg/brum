package brum

import java.{util => ju}

case class Document(
    input: Input,
    definitions: ju.LinkedHashSet[String] = new ju.LinkedHashSet(),
    references: ju.LinkedHashSet[String] = new ju.LinkedHashSet()
) {
  def addReference(qualifier: String, name: String): String = {
    val fqn = s"$qualifier.$name"
    references.add(fqn)
    fqn
  }
}
