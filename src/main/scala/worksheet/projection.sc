import geotrellis.proj4._

val wgs84 = LatLng
val utm15N = CRS.fromEpsgCode(2027)
val southPA = CRS.fromString("+proj=lcc +lat_1=40.96666666666667 +lat_2=39.93333333333333 +lat_0=39.33333333333334 +lon_0=-77.75 +x_0=600000 +y_0=0 +ellps=GRS80 +datum=NAD83 +to_meter=0.3048006096012192 +no_defs")

import geotrellis.vector._

val philly = Point(-75.1652,39.9526)

philly.reproject(LatLng, southPA)

import geotrellis.raster._
val tile = ArrayTile(Array.fill(4*4)(1), 4, 4)
val extent = philly.buffer(0.01).extent
val raster = Raster(tile, extent)

raster.reproject(LatLng, southPA)

