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

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      val context = new GeoApiContext().setApiKey(Play.current.configuration.getString("google.api.token").get)

      val source = Source.fromFile(picture.ref.file)
      source.getLines().next // skip first line
      val points = source.getLines().map {
        line => parseLineToLatLng(line)
      }.toList

      val results3 = ElevationApi.getByPoints(context, new EncodedPolyline(points.slice(0, 250).asJava)).await()

      val a = Enumerator.enumerate(results3.map({
        a => s"${a.location.lat};${a.location.lng};${a.elevation};${a.resolution}\n"
      }))

      Ok.chunked(a.andThen(Enumerator.eof)).withHeaders(
        "Content-Type" -> "txt/plain",
        "Content-Disposition" -> s"attachment; filename=elevation.txt"
      )
    }.get
  }


  def parseLineToLatLng(line: String): LatLng = {
    val SEPARATOR = " "

    val point = line.split(SEPARATOR)
    new LatLng(parseFloat(point(0)), parseFloat(point(1)))
  }

  def parseFloat(s: String): Float = {
    val SCALE = 4

    BigDecimal(s.trim).setScale(SCALE, RoundingMode.DOWN).floatValue()
  }

}