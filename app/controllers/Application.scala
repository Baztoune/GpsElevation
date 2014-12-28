package controllers

import com.google.maps.model.{EncodedPolyline, LatLng}
import com.google.maps.{ElevationApi, GeoApiContext}
import play.api.Play
import play.api.libs.iteratee.Enumerator
import play.api.mvc._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.math.BigDecimal.RoundingMode


object Application extends Controller {
  val SEPARATOR = " "
  val SCALE = 4

  def index = Action {
    Ok(views.html.index(None, None))
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("inputGpsData").map { inputGpsData =>

      // Get token
      val context = new GeoApiContext().setApiKey(Play.current.configuration.getString("google.api.token").get)

      // Parse file
      val source = Source.fromFile(inputGpsData.ref.file)
      source.getLines().next // skip first line
      val uniqueLatLngs = source.getLines().map {
        line => parseLineToLatLng(line)
      }.toList.distinct.map({uniqueLatLngToNative(_)})

      // TODO loop by blocks to prevent long http queries
      val results = ElevationApi.getByPoints(context, new EncodedPolyline(uniqueLatLngs.slice(0,250).asJava)).await()

      // Map and stream results
      val resultsEnumerator = Enumerator.enumerate(results.map({
        result => s"${result.location.lat};${result.location.lng};${result.elevation};${result.resolution}\n"
      }))

      Ok.chunked(resultsEnumerator.andThen(Enumerator.eof)).withHeaders(
        "Content-Type" -> "txt/plain",
        "Content-Disposition" -> s"attachment; filename=elevation.txt"
      )
    }.get
  }

  /**
   * We need this case class to use List.distinct, otherwise it doesn't work
   * @param lat
   * @param lng
   */
  case class LatLngCaseClass(lat:Double, lng:Double)
  def uniqueLatLngToNative(point: LatLngCaseClass): LatLng = {
    new LatLng(point.lat,point.lng)
  }

  /**
   * Parse the entire line and return a LatLngCaseClass point
   * @param line
   * @return the point
   */
  def parseLineToLatLng(line: String): LatLngCaseClass = {
    val point = line.split(SEPARATOR)
    LatLngCaseClass(parseFloat(point(0)), parseFloat(point(1)))
  }


  /**
   * Parse a String to a Float, using the defined SCALE
   * @param s
   * @return float
   */
  def parseFloat(s: String): Float = {
    BigDecimal(s.trim).setScale(SCALE, RoundingMode.DOWN).floatValue()
  }

}