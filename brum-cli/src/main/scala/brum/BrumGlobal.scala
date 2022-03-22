package brum

import java.{util => ju}
import scala.collection.mutable.ListBuffer
import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.reporters.Reporter

class BrumGlobal(settings: Settings, reporter: Reporter)
    extends Global(settings, reporter) {
  def index(input: Input): Document = {
    val doc = Document(input)
    val unit = this.newCompilationUnit(input.text, input.filename)
    val tree = this.parseTree(unit.source)
    new DocumentTraverser(doc).traverse(tree)
    // pprint.log(doc)
    doc
  }

  private class DocumentTraverser(doc: Document) extends Traverser {
    private val scope = new ju.LinkedHashMap[Name, String]
    private var currentPackage = List.empty[String]
    override def traverse(tree: Tree): Unit = {
      tree match {
        case PackageDef(pid, stats) =>
          val fqn = fullyQualifiedName(pid).mkString(".")
          currentPackage ::= fqn
          stats.foreach(traverse)
          currentPackage = currentPackage.tail
        case ClassDef(mods, name, _, _) =>
          currentPackage ::= name.toString()
          doc.definitions.add(currentPackage.reverseIterator.mkString("."))
          mods.annotations.foreach(traverse)
          super.traverse(tree)
          currentPackage = currentPackage.tail
        case ValOrDefDef(mods, _, _, _) =>
          mods.annotations.foreach(traverse)
          super.traverse(tree)
        case Import(expr, selectors) =>
          val qualifierFqn = fullyQualifiedName(expr)
          selectors.foreach { selector =>
            if (selector.name.toString() == "_") {
              doc.references.add(qualifierFqn.mkString("."))
            } else {
              val fqn =
                qualifierFqn.mkString("", ".", "." + selector.name.toString())
              doc.references.add(fqn)
              val enteredName =
                if (selector.rename != null) selector.rename else selector.name
              scope.put(enteredName, fqn)
            }
          }
        case _: Select =>
          fullyQualifiedName(tree) match {
            case fqn @ (name :: tail) =>
              val qualifier = scope.get(name)
              if (qualifier != null) {
                doc.references.add(tail.mkString(qualifier + ".", ".", ""))
              } else {
                doc.references.add(fqn.mkString("."))
              }
            case Nil =>
              super.traverse(tree)
          }
        case _ =>
          super.traverse(tree)
        //   println(tree.getClass())
        //   println(tree)
      }
    }

    private def fullyQualifiedName(tree: Tree): List[Name] = {
      val result = ListBuffer.empty[Name]
      def loop(x: Tree): Boolean = x match {
        case Ident(name) =>
          result += name
          true
        case Select(expr, name) =>
          if (loop(expr)) {
            result += name
            true
          } else {
            false
          }
        case _ =>
          false
      }
      if (loop(tree)) result.toList
      else Nil
    }
  }
}
