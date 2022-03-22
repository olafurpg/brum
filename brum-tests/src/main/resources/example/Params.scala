package example

import scala.collection.mutable.TreeMap

class Params(a: String) {
  def method(b: String): Unit = {
    a.linesIterator ++ b.linesIterator
  }
}
