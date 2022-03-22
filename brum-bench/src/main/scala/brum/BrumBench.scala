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

case class Corpus(url: String) {
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
      "https://github.com/typelevel/cats/archive/219fcfbc27a354ef2ace11b5a86e2f2590bded79.zip"
    )
  val scala =
    Corpus(
      "https://github.com/scala/scala/archive/c8ee9915b35c2a9a6f039815b0d55e397e627034.zip"
    )
  val spark =
    Corpus(
      "https://github.com/apache/spark/archive/99992a4e050a00564049be6938f5734876c17518.zip"
    )
  val all = List(paiges, cats, scala, spark)
}

@State(Scope.Benchmark)
class BrumBench {
  @Setup
  def setup(): Unit = {
    Corpus.all.foreach { corpus =>
      corpus.localPath()
    }
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SingleShotTime))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def singleShot: Unit = {
    val inputs = Input
      .fromZipFile(Corpus.spark.localPath())
      .filterNot(_.filename.contains("scala-3"))
    val options = Options(inputs)
    val docs = Brum.run(options)
    val lines = inputs.iterator.map(_.text.count(_ == '\n')).sum
    pprint.log(lines)
    pprint.log(docs.last)
  }
}
