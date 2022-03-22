package brum

import java.{util => ju}
import scala.collection.mutable.ListBuffer
import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.reporters.Reporter
import scala.annotation.meta.param

class BrumGlobal(settings: Settings, reporter: Reporter)
    extends Global(settings, reporter) {
  def index(input: Input): Document = {
    val doc = Document(input)
    val unit = this.newCompilationUnit(input.text, input.filename)
    val tree = this.parseTree(unit.source)
    new DocumentTraverser(doc).traverse(tree)
    doc
  }

  private class DocumentTraverser(doc: Document) extends Traverser {
    private val scope = new ju.LinkedHashMap[Name, String]
    private val locals = new ju.HashMap[Name, Integer]
    private var currentPackage = List.empty[String]
    private object ClassOrModuleDef {
      def unapply(tree: Tree): Option[(Modifiers, Name, Template)] =
        tree match {
          case ModuleDef(mods, name, template)   => Some((mods, name, template))
          case ClassDef(mods, name, _, template) => Some((mods, name, template))
          case _                                 => None
        }
    }
    def withLocals(names: Array[Name])(thunk: => Unit): Unit = {
      addLocals(names)
      thunk
      removeLocals(names)
    }
    def addLocals(names: Array[Name]): Unit = {
      names.foreach { name =>
        val old = locals.get(name)
        if (old == null) {
          locals.put(name, 1)
        } else {
          locals.put(name, old + 1)
        }
      }
    }
    def removeLocals(names: Array[Name]): Unit = {
      names.foreach { name =>
        val old = locals.get(name)
        if (old != null && old > 0) {
          locals.put(name, old - 1)
        }
      }
    }
    override def traverse(tree: Tree): Unit = {
      tree match {
        case PackageDef(pid, stats) =>
          val fqn = fullyQualifiedName(pid).mkString(".")
          currentPackage ::= fqn
          stats.foreach(traverse)
          currentPackage = currentPackage.tail
        case Template(_, _, body) =>
          val members = body.iterator.collect { case d: DefTree =>
            d.name
          }.toArray
          pprint.log(members)
          withLocals(members) {
            super.traverse(tree)
            members.foreach(locals.remove(_))
          }
        case ClassOrModuleDef(mods, name, template) =>
          mods.annotations.foreach(traverse)
          val isNamed = !name.endsWith("$anon")
          if (isNamed) {
            currentPackage ::= name.toString()
            doc.definitions.add(currentPackage.reverseIterator.mkString("."))
          }
          traverse(template)
          if (isNamed) {
            currentPackage = currentPackage.tail
          }
        case ValOrDefDef(mods, name, _, _) =>
          val parameters: Array[Name] = tree match {
            case d: DefDef => d.vparamss.iterator.flatten.map(_.name).toArray
            case _         => Array()
          }
          pprint.log(name -> parameters)
          mods.annotations.foreach(traverse)
          withLocals(parameters) {
            super.traverse(tree)
          }
        case Block(stats, expr) =>
          val variables: Array[Name] = stats.iterator.collect {
            case d: DefTree => d.name
          }.toArray
          withLocals(variables) {
            super.traverse(tree)
          }
        case Function(vparams, _) =>
          val params = vparams.iterator.map(_.name: Name).toArray
          withLocals(params) {
            super.traverse(tree)
          }
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
        case _: Select | _: Ident =>
          fullyQualifiedName(tree) match {
            case fqn @ (name :: tail) =>
              val qualifier = scope.get(name)
              val local = locals.get(name)
              val isLocal = local != null && local > 0
              pprint.log(name -> isLocal -> locals)
              if (isLocal) {
                () // do nothing
              } else if (qualifier != null) {
                doc.references.add(tail.mkString(qualifier + ".", ".", ""))
              } else if (!name.endsWith("$anon")) {
                if (fqn.lengthCompare(1) > 0) {
                  doc.references.add(fqn.mkString("."))
                }
                doc.references.add(
                  fqn.mkString(
                    currentPackage.reverseIterator.mkString("", ".", "."),
                    ".",
                    ""
                  )
                )
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
