

import geotrellis.layer.LayoutDefinition
import geotrellis.raster.geotiff.GeoTiffRasterSource
import geotrellis.spark._
import geotrellis.vector._
import geotrellis.vector.io.json.JsonFeatureCollection
import org.apache.spark._

implicit val sc = {
  val conf = (new SparkConf)
    .setMaster("local[16]")
    .setAppName("worksheet")
    .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .set("spark.kryo.registrator", classOf[geotrellis.spark.store.kryo.KryoRegistrator].getName)

  SparkContext.getOrCreate(conf)
}

//TODO: place data files on S3 bucket
//TODO: move to Jupyter Notebook

val uri = "/Users/eugene/data/facebook/hrpd/USA_lat_24_lon_-81_elderly_60_plus.tif"
val source = GeoTiffRasterSource(uri)
val layout = LayoutDefinition(source.gridExtent, 1024)
val layer = RasterSourceRDD.spatial(List(source), layout)

// reading in the Features>import java.nio.file.{ Paths, Files }
import scala.collection.JavaConverters._
import java.nio.file.{Paths, Files}
val countiesPath = Paths.get("/Users/eugene/proj/covid19-healthsystemcapacity/data/external/us_counties.geojson")
val countiesJson = Files.readAllLines(countiesPath).asScala.mkString
val fc = countiesJson.parseGeoJson[JsonFeatureCollection]


case class CountyInfo(GEO_ID: String)

import _root_.io.circe._, _root_.io.circe.generic.semiauto._
implicit val fooDecoder: Decoder[CountyInfo] = deriveDecoder[CountyInfo]
implicit val fooEncoder: Encoder[CountyInfo] = deriveEncoder[CountyInfo]

val features = fc.getAllPolygonFeatures[CountyInfo]()
val featuresRdd = sc.parallelize(features, 10)

import geotrellis.raster.summary.polygonal.visitors._
import geotrellis.raster.rasterize.Rasterizer

val s = example.PolygonalSummary.forFeatures(
  layer,
  featuresRdd,
  new SumVisitor.MultibandTileSumVisitor,
  Rasterizer.Options.DEFAULT)

val win = s.collect().map { case (id, feature) =>
  feature.mapData(r => (id, r.toOption.map(_.apply(0).value)))
}

val winstring = JsonFeatureCollection(win).toString
Files.write(Paths.get("/tmp/winning.geojson"), winstring.getBytes)

sc.stop()