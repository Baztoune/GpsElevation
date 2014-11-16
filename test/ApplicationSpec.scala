
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._

import scala.math.BigDecimal.RoundingMode

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the index page" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain("Your new application is ready.")
    }

    "read file" in new WithApplication() {

      import scala.io.Source

      val source = Source.fromURL(getClass.getResource("/resources/longitude_latitude.txt"))


      source.getLines().next // skip first line
      source.getLines().foreach(
        l => println(parseLine(l))
      )

      source.getLines().hasNext should beFalse
    }
  }

  case class Geo(lat: Float, lon: Float, elevation: Option[Float])
  val SEPARATOR = " "
  val SCALE = 4

  def parseLine(line: String): Geo = {
    val lat = line.split(SEPARATOR)
    Geo(parseFloat(lat(0)), parseFloat(lat(1)), null)
  }

  def parseFloat(s:String) : Float = {
    BigDecimal(s.trim).setScale(SCALE, RoundingMode.DOWN).floatValue()
  }

}
