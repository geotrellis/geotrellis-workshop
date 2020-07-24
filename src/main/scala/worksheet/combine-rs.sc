import geotrellis.raster.geotiff.GeoTiffRasterSource
import geotrellis.raster._
import workshop.Util
import geotrellis.proj4._

//def bandURI(band: String) = s"s3://geotrellis-workshop/landsat/LC81070352015218LGN00_$band.TIF"
def bandURI(band: String) = s"/Users/eugene/workshop-data/landsat/LC81070352015218LGN00_$band.TIF"
val greenBand = GeoTiffRasterSource(bandURI("B3"))

val multiband = GeoTiffRasterSource("s3://geotrellis-workshop/ta/multiband-cog.tif")
val lc8URI = "s3://landsat-pds/c1/L8/200/034/LC08_L1TP_200034_20200611_20200625_01_T1/LC08_L1TP_200034_20200611_20200625_01_T1_B3.TIF"
val landsat = GeoTiffRasterSource(lc8URI)

landsat.extent