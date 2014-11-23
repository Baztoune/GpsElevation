
import com.google.maps.model.LatLng
import com.google.maps.{ElevationApi, GeoApiContext}
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
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
    "read and parse the file" in new WithApplication() {

      import scala.io.Source

      val source = Source.fromURL(getClass.getResource("/resources/longitude_latitude.txt"))

      source.getLines().next // skip first line
      source.getLines().foreach(
        line => println(parseLine(line))
      )

      source.getLines().hasNext should beFalse
    }

    "Call the WS and get values" in new WithApplication() {
      val SYDNEY = new LatLng(-33.867487, 151.206990)
      val MELBOURNE = new LatLng(-37.814107, 144.963280)

      val context = new GeoApiContext().setApiKey("AIzaSyAkQPEF_Wk3lFD6VnRaG-SyAQyHLd2jP9c")

      val results2 = ElevationApi.getByPoint(context, new LatLng(48.81635618944209, 2.206782968924472)).await()
      val results3 = ElevationApi.getByPoints(context, SYDNEY, MELBOURNE).await()

      results3.foreach(
        p => println(s"${p.location.lat};${p.location.lat} => ${p.elevation}")
      )

      results2 should not be empty
      results3 should not be empty
    }
  }

  case class Geo(lat: Float, lon: Float, elevation: Option[Float])

  val SEPARATOR = " "
  val SCALE = 4

  def parseLine(line: String): Geo = {
    val lat = line.split(SEPARATOR)
    Geo(parseFloat(lat(0)), parseFloat(lat(1)), null)
  }

  def parseFloat(s: String): Float = {
    BigDecimal(s.trim).setScale(SCALE, RoundingMode.DOWN).floatValue()
  }

}
