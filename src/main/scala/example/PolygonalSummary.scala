package example

import java.util.UUID

import cats.{Eq, Semigroup}
import cats.syntax.semigroup._
import geotrellis.layer.{Metadata, SpatialKey, TileLayerMetadata}
import geotrellis.raster._
import geotrellis.raster.rasterize.Rasterizer
import geotrellis.raster.summary.GridVisitor
import geotrellis.raster.summary.polygonal._
import geotrellis.util.MethodExtensions
import geotrellis.vector._
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

/**
 * GeoTrellis Polygonal Summary works on input of RDD[Geometry]
 * - There is no way to preserve the feature ID through the polygonal summary
 * https://github.com/locationtech/geotrellis/blob/master/spark/src/main/scala/geotrellis/spark/summary/polygonal/RDDPolygonalSummary.scala
 *
 * This is a good example of using Spark + GT primitives to extend GT features
 */
object PolygonalSummary {
  def forFeatures[R, ID, G <: Geometry, T <: CellGrid[Int]](
    rasterRdd: RDD[(SpatialKey, T)] with Metadata[TileLayerMetadata[SpatialKey]],
    featureRdd: RDD[Feature[G, ID]],
    visitor: GridVisitor[Raster[T], R],
    options: Rasterizer.Options
  )(implicit
    ev0: Semigroup[R],
    ev2: ClassTag[R],
    ev3: ClassTag[ID]
  ): RDD[(ID, Feature[G, PolygonalSummaryResult[R]])] = {
    val layout = rasterRdd.metadata.layout

    val keyedGeometryRdd: RDD[(SpatialKey, Feature[G, ID])] =
      featureRdd.flatMap { feature =>
        val keys: Set[SpatialKey] = layout.mapTransform.keysForGeometry(feature.geom)
        keys.map(k => (k, feature))
      }

    // Combine geometries and raster by SpatialKey, then loop partitions
    //  and perform the polygonal summary on each combination
    val joinedRdd = keyedGeometryRdd.join(rasterRdd)
    val featuresWithSummaries: RDD[(ID, Feature[G, PolygonalSummaryResult[R]])] =
      joinedRdd.map { case (spatialKey, (Feature(geom, id), tile)) =>
        val extent: Extent = layout.mapTransform.keyToExtent(spatialKey)
        val raster: Raster[T] = Raster(tile, extent)
        val partialResult: PolygonalSummaryResult[R] =
          raster.polygonalSummary(geom, visitor.getClass.newInstance, options)
        (id, Feature(geom, partialResult))
      }

    // Geometries may have been split across SpatialKey leading to multiple UUID keys.
    // Combine by UUID and then drop the UUID, which was added within this method
    featuresWithSummaries
      .reduceByKey { (a, b) => Feature(a.geom, a.data.combine(b.data)) }
  }
}
