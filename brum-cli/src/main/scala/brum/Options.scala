package brum

import java.nio.file.Paths

case class Options(files: collection.Seq[Input])

object Options {
  def fromArguments(args: Array[String]): Options = {
    Options(args.map(path => Input.fromFile(Paths.get(path))).toList)
  }
}
