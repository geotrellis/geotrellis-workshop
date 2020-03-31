import geotrellis.raster._
import geotrellis.raster.resample._
import geotrellis.vector._
import geotrellis.proj4._
import geotrellis.layer._
import geotrellis.spark._
import geotrellis.spark.store.hadoop.HadoopGeoTiffRDD
import geotrellis.spark.store.s3.S3GeoTiffRDD
import org.apache.hadoop.fs.Path
import org.apache.spark._
import org.apache.spark.rdd._

implicit val sparkContext =
  SparkContext.getOrCreate(
    new SparkConf(loadDefaults = true)
      .setMaster("local[*]")
      .setAppName("Demo")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryo.registrator", "geotrellis.spark.store.kryo.KryoRegistrator")
  )

val inputRdd = S3GeoTiffRDD.spatial("geotrellis-demo", "cogs/harrisburg-pa/elevation.tif")

// Use the "TileLayerMetadata.fromRdd" call to find the zoom
// level that the closest match to the resolution of our source image,
// and derive information such as the full bounding box and data type.
val (_, rasterMetaData) = CollectTileLayerMetadata.fromRDD(inputRdd, FloatingLayoutScheme(256))

// Use the Tiler to cut our tiles into tiles that are index to a floating layout scheme.
// We'll repartition it so that there are more partitions to work with, since spark
// likes to work with more, smaller partitions (to a point) over few and large partitions.
val tiled: RDD[(SpatialKey, Tile)] =
  inputRdd.tileToLayout(rasterMetaData.cellType, rasterMetaData.layout, Bilinear)

// We'll be tiling the images using a zoomed layout scheme
// in the web mercator format (which fits the slippy map tile specification).
// We'll be creating 256 x 256 tiles.
val layoutScheme = ZoomedLayoutScheme(WebMercator, tileSize = 256)

// We need to reproject the tiles to WebMercator
val (zoom, reprojected): (Int, RDD[(SpatialKey, Tile)] with Metadata[TileLayerMetadata[SpatialKey]]) =
  TileLayerRDD(tiled, rasterMetaData).reproject(WebMercator, layoutScheme, Bilinear)

sparkContext.stop()
