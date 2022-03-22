package brum

import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.ConsoleReporter
import scala.util.control.NonFatal
import scala.collection.mutable.ArrayBuffer

object Brum {
  def main(args: Array[String]): Unit = {
    val options = Options.fromArguments(args)
    val docs = run(options)
    docs.foreach { doc => println(doc.toJSON.renderAsString) }
  }
  def run(options: Options): ArrayBuffer[Document] = {
    val settings = new Settings()
    settings.usejavacp.value = true
    settings.stopAfter.value = List("parser")
    val global = new BrumGlobal(settings, new ConsoleReporter(settings))
    val result = ArrayBuffer.empty[Document]
    options.files.foreach { path =>
      try {
        result += global.index(path)
      } catch {
        case NonFatal(e) =>
          println(s"error: failed to index document $path")
          e.printStackTrace()
      }
    }
    global.askShutdown()
    result
  }
}
