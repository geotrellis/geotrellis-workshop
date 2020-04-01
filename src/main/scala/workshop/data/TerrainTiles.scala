package workshop.data

import geotrellis.layer.{LayoutDefinition, SpatialKey}
import geotrellis.raster.{RasterSource, TileLayout}
import geotrellis.raster.gdal.GDALRasterSource
import geotrellis.vector.{Extent, Geometry}

/** Mapzen Terrain Tiles
 * @see https://registry.opendata.aws/terrain-tiles
 */
object TerrainTiles {
  val layout: LayoutDefinition = {
    val worldExtent = Extent(-180.0000, -90.0000, 180.0000, 90.0000)
    val tileLayout = TileLayout(layoutCols = 360, layoutRows = 180, tileCols = 3601, tileRows = 3601)
    LayoutDefinition(worldExtent, tileLayout)
  }

  /** Skadi raster for each a 1x1 degree tile */
  def tileUri(key: SpatialKey): String = {
    val col = key.col - 180
    val long = if (col >= 0) f"E${col}%03d" else f"W${-col}%03d"

    val row = 89 - key.row
    val lat = if (row >= 0) f"N${row}%02d" else f"S${-row}%02d"

    f"s3://elevation-tiles-prod/v2/skadi/$lat/$lat$long.hgt.gz"
  }

  def getRasterSource(tileKey: SpatialKey): RasterSource = GDALRasterSource(tileUri(tileKey))

  def getRasterSource(tileKeys: Set[SpatialKey]): Seq[RasterSource] = tileKeys.toSeq.map({ key => GDALRasterSource(tileUri(key)) })

  def getRasterSource(geom: Geometry): Seq[RasterSource] = {
    val tileKeys = layout.mapTransform.keysForGeometry(geom)
    tileKeys.toSeq.map({ key => GDALRasterSource(tileUri(key)) })
  }
}
