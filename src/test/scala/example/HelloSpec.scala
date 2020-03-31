package example

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class HelloSpec extends AnyFlatSpec with Matchers {
  "The Hello object" should "say hello" in {
    "hello" shouldEqual "hello"
  }
}
