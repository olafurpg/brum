package brum

import java.{util => ju}

case class Document(
    input: Input,
    definitions: ju.LinkedHashSet[String] = new ju.LinkedHashSet(),
    references: ju.LinkedHashSet[String] = new ju.LinkedHashSet()
) {
  def snapshotString: String = {
    val out = new StringBuilder
    definitions.forEach { definition =>
      out.addAll("definition ").addAll(definition).addOne('\n')
    }
    references.forEach { reference =>
      out.addAll("reference ").addAll(reference).addOne('\n')
    }
    out.toString()
  }
  def toJSON: JSON = {
    val members = new ju.LinkedHashMap[String, JSON]()

    members.put("file", JSON.String(input.filename))

    val jsonDefinitions = new ju.ArrayDeque[JSON](definitions.size())
    definitions.forEach { d =>
      jsonDefinitions.add(JSON.String(d))
    }
    members.put("definitions", JSON.Array(jsonDefinitions))

    val jsonReferences = new ju.ArrayDeque[JSON](references.size())
    references.forEach { r =>
      jsonReferences.add(JSON.String(r))
    }
    members.put("references", JSON.Array(jsonReferences))

    JSON.Object(members)
  }

  def addReference(qualifier: String, name: String): String = {
    val fqn = s"$qualifier.$name"
    references.add(fqn)
    fqn
  }
}
