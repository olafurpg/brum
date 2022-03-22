package example

import scala.collection.mutable.{ArrayBuffer, TreeMap}
import scala.math.BigDecimal._
import scala.concurrent

@scala.annotation.strictfp()
class Example(buf: ArrayBuffer[Int], t: TreeMap[Int, Int])
    extends scala.math.Ordered[Int] {
  def member1 = defaultMathContext
  @scala.annotation.compileOnly
  def member2 = concurrent.Future
  def member3(p: List[Int]): scala.util.Success[Int] = {
    new Ordering[Int] {}
    p.head
  }
  class Inner()
  object Inner
}

object Example {
  class CompanionInner
  object CompanionInner2
}
