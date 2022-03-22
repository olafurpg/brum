package brum

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile
import scala.collection.mutable.ArrayBuffer

case class Input private (filename: String, text: String, path: Option[Path]) {}

object Input {
  def fromText(filename: String, text: String): Input = {
    Input(filename = filename, text = text, path = None)
  }
  def fromZipFile(file: Path): ArrayBuffer[Input] = {
    val jar = new JarFile(file.toFile())
    try {
      val entries = jar.entries()
      val result = ArrayBuffer.empty[Input]
      while (entries.hasMoreElements()) {
        val element = entries.nextElement()
        if (element.getName().endsWith(".class")) {
          result += Input.fromText(element.getName(), "")
        } else if (element.getName().endsWith(".scala")) {
          val in = jar.getInputStream(element)
          val bytes = readBytes(in)
          result += Input.fromText(
            element.getName(),
            new String(bytes, StandardCharsets.UTF_8)
          )
        }
      }
      result
    } finally {
      jar.close()
    }
  }
  def fromFile(file: Path): Input = {
    Input(
      filename = file.toString(),
      text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8),
      path = Some(file)
    )
  }
  private def readBytes(is: InputStream): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val buffer = new Array[Byte](4096)
    var nread = -1
    do {
      nread = is.read(buffer, 0, buffer.length)
      if (nread != -1) baos.write(buffer, 0, nread)
    } while (nread != -1)
    baos.toByteArray
  }
}
