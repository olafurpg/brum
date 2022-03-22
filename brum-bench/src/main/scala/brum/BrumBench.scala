package brum

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.AuxCounters
import org.openjdk.jmh.annotations.Threads

case class Corpus(url: String, ignorePatterns: List[String] = Nil) {
  def isIgnored(filename: String): Boolean =
    ignorePatterns.exists(pattern => filename.contains(pattern))
  def localPath(): Path = {
    val path = Paths.get(
      System.getProperty("java.io.tmpdir"),
      "brum-scala",
      math.abs(url.hashCode()) + ".zip"
    )
    if (!Files.isRegularFile(path)) {
      println(s"Downloading... $url")
      Files.createDirectories(path.getParent())
      Files.copy(new URL(url).openConnection().getInputStream(), path)
    }
    path
  }
}

object Corpus {
  val paiges =
    Corpus(
      "https://github.com/typelevel/paiges/archive/544efcc43dcf788df4188da48e93650bd02b465e.zip"
    )
  val cats =
    Corpus(
      "https://github.com/typelevel/cats/archive/219fcfbc27a354ef2ace11b5a86e2f2590bded79.zip",
      ignorePatterns = List("scala-3")
    )
  val scala =
    Corpus(
      "https://github.com/scala/scala/archive/c8ee9915b35c2a9a6f039815b0d55e397e627034.zip",
      ignorePatterns = List("test/files", "tasty", "src-3")
    )
  val spark =
    Corpus(
      "https://github.com/apache/spark/archive/99992a4e050a00564049be6938f5734876c17518.zip"
    )
  val all = List(paiges, cats, scala, spark)
}
object Counters {
  @AuxCounters(AuxCounters.Type.EVENTS)
  @State(Scope.Thread)
  class AdditionalCounters {
    var linesOfCode: Long = _
    @Setup()
    def setup(): Unit = {
      linesOfCode = 0
    }
  }
}

@State(Scope.Thread)
@Threads(1)
class BrumBench {
  @Setup
  def setup(): Unit = {
    Corpus.all.foreach { corpus =>
      corpus.localPath()
    }
  }
  @Param(Array("spark", "scala", "cats", "paiges"))
  var corpus: String = _

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def indexCorpus(counters: Counters.AdditionalCounters): Unit = {
    val Some(c) = Corpus.all.find(_.url.contains(corpus))
    val inputs = Input
      .fromZipFile(c.localPath())
      .filterNot(path => c.isIgnored(path.filename))
    val options = Options(inputs)
    val docs = Brum.run(options)
    val lines = inputs.iterator.map(_.text.count(_ == '\n')).sum
    counters.linesOfCode = lines
  }
}
