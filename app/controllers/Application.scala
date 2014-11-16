package controllers

import com.google.maps.model.LatLng
import com.google.maps.{ElevationApi, GeoApiContext, GeocodingApi}
import play.api.mvc._


object Application extends Controller {

  def index = Action {

    val SYDNEY = new LatLng(-33.867487, 151.206990)
    val MELBOURNE = new LatLng(-37.814107, 144.963280)

    val context = new GeoApiContext().setApiKey("AIzaSyAkQPEF_Wk3lFD6VnRaG-SyAQyHLd2jP9c")

    val results = GeocodingApi.geocode(context, "1600 Amphitheatre Parkway Mountain View, CA 94043").await()
    val results2 = ElevationApi.getByPoint(context, new LatLng(48.81635618944209,2.206782968924472)).await()
    val results3 = ElevationApi.getByPoints(context, SYDNEY, MELBOURNE).await()

    println(results2.elevation)
    results3.foreach(p => println(s"${p.location.lat};${p.location.lat} => ${p.elevation}"))

    Ok(views.html.index("Your new application is ready."))
  }

}