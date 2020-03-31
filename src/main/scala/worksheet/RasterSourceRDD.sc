import geotrellis.layer._
import geotrellis.raster.geotiff.GeoTiffRasterSource
import geotrellis.spark._
import org.apache.spark._

implicit val sparkContext =
  SparkContext.getOrCreate(
    new SparkConf(loadDefaults = true)
      .setMaster("local[*]")
      .setAppName("Demo")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryo.registrator", "geotrellis.spark.store.kryo.KryoRegistrator")
  )
sparkContext.uiWebUrl

val uri = "s3://geotrellis-demo/cogs/harrisburg-pa/elevation.tif"
val source = GeoTiffRasterSource(uri)

val summary = RasterSummary.fromSeq(List(source))
val layout = summary.layoutDefinition(FloatingLayoutScheme(256))

val layer = RasterSourceRDD.spatial(source, layout)

sparkContext.stop()
