package controllers

import com.google.maps.model.{EncodedPolyline, LatLng}
import com.google.maps.{ElevationApi, GeoApiContext}
import org.joda.time.DateTime
import play.api.Play
import play.api.libs.iteratee.Enumerator
import play.api.mvc._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.math.BigDecimal.RoundingMode


object Application extends Controller {
  val DEFAULT_SEPARATOR = " "
  val DEFAULT_SCALE = 4

  def index = Action {
    Ok(views.html.index())
  }

  def upload = Action(parse.multipartFormData) { request =>
    val scale = request.body.dataParts.getOrElse("scale", Seq.empty).headOption.getOrElse(DEFAULT_SCALE.toString).toInt
    val dateAsText = DateTime.now().toString("yyyyMMdd-HH'h'mm'm'ss's'")

    request.body.file("inputGpsData").map { inputGpsData =>
      // Get token
      val context = new GeoApiContext().setApiKey(Play.current.configuration.getString("google.api.token").get)

      // Parse file
      val source = Source.fromFile(inputGpsData.ref.file)
      source.getLines().next // skip first line

      // Map results, init enumerator
      val resultsEnumerator = Enumerator.enumerate(
        source.getLines()
          .map(parseLineToLatLng(_, scale))                                                                           // parse
          .toList.distinct                                                                                            // do not keep duplicates
          .map(uniqueLatLngToNative)                                                                                  // convert to LatLng
          .grouped(250)                                                                                               // group http requests
          .flatMap(points => ElevationApi.getByPoints(context, new EncodedPolyline(points.asJava)).await())           // call API
          .map(result => s"${result.location.lat};${result.location.lng};${result.elevation};${result.resolution}\n") // format result
      )

      // Stream results to file
      Ok.chunked(resultsEnumerator.andThen(Enumerator.eof))
        .withHeaders(
          "Content-Type" -> "txt/plain",
          "Content-Disposition" -> s"attachment; filename=elevation-$dateAsText-precision$scale.txt"
        )
    }.get
  }

  /**
   * We need this case class to use List.distinct, otherwise it doesn't work
   */
  case class LatLngCaseClass(lat: Double, lng: Double)

  def uniqueLatLngToNative(point: LatLngCaseClass): LatLng = {
    new LatLng(point.lat, point.lng)
  }

  /**
   * Parse the entire line and return a LatLngCaseClass point
   * @param line
   * @return the point
   */
  def parseLineToLatLng(line: String, scale:Int): LatLngCaseClass = {
    val geoPoint = line.split(DEFAULT_SEPARATOR)
    LatLngCaseClass(parseDouble(geoPoint(0), scale), parseDouble(geoPoint(1), scale))
  }


  /**
   * Parse a String to a Float, using the defined SCALE
   * @param s
   * @return float
   */
  def parseDouble(s: String, scale: Int): Double = {
    BigDecimal(s.trim).setScale(scale, RoundingMode.HALF_DOWN).rounded.doubleValue()
  }

}