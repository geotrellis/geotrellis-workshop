import geotrellis.vector._
import geotrellis.raster._
import geotrellis.raster.geotiff.GeoTiffRasterSource
import geotrellis.raster.summary.polygonal.visitors._
import geotrellis.raster.rasterize.Rasterizer
import workshop.Util


val raster: Raster[Tile] = ???
val geom: Geometry = ???

import geotrellis.raster.summary.polygonal._

val mean = raster.polygonalSummary(
  geom,
  new MeanVisitor.TileMeanVisitor,
  Rasterizer.Options.DEFAULT)
