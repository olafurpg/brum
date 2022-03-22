package brum

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class DeleteVisitor extends SimpleFileVisitor[Path] {
  override def visitFile(
      file: Path,
      attrs: BasicFileAttributes
  ): FileVisitResult = {
    Files.delete(file)
    super.visitFile(file, attrs)
  }
  override def postVisitDirectory(
      dir: Path,
      exc: IOException
  ): FileVisitResult = {
    Files.delete(dir)
    super.postVisitDirectory(dir, exc)
  }
}
