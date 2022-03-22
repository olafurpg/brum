package brum

import java.nio.file.Files
import java.nio.file.SimpleFileVisitor
import java.nio.file.Path
import java.nio.file.FileVisitResult
import java.nio.file.attribute.BasicFileAttributes
import java.nio.charset.StandardCharsets

class BrumSuite extends munit.FunSuite {
  val resourceDirectory = TestBuildInfo.resourceDirectory.toPath
  val snapshotDirectory = TestBuildInfo.snapshotDirectory.toPath
  def updateSnapshots = false
  if (System.getenv("CI") != null && updateSnapshots) {
    sys.error("updateSnapshots cannot be true inside CI.")
  }
  if (updateSnapshots) {
    Files.walkFileTree(snapshotDirectory, new DeleteVisitor)
  }

  Files.walkFileTree(
    resourceDirectory,
    new SimpleFileVisitor[Path] {
      override def visitFile(
          file: Path,
          attrs: BasicFileAttributes
      ): FileVisitResult = {
        runTest(file)
        super.visitFile(file, attrs)
      }
    }
  )

  def runTest(file: Path): Unit = {
    val relativeFile = resourceDirectory.relativize(file)
    test(relativeFile.toString()) {
      val input = Input.fromFile(file)
      val collection.Seq(doc) = Brum.run(Options(List(input)))
      val obtained = doc.snapshotString
      val snapshotFile = snapshotDirectory
        .resolve(relativeFile)
        .resolveSibling(file.getFileName().toString() + ".snapshot")
      if (updateSnapshots) {
        Files.createDirectories(snapshotFile.getParent)
        Files.write(snapshotFile, obtained.getBytes(StandardCharsets.UTF_8))
      } else {
        val expected =
          if (Files.isRegularFile(snapshotFile))
            Input.fromFile(snapshotFile).text
          else ""
        assertNoDiff(obtained, expected)
      }
    }
  }

}
