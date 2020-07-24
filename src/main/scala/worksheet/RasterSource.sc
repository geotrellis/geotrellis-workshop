import geotrellis.layer.{LayoutDefinition, LayoutTileSource, SpatialKey, ZoomedLayoutScheme}
import geotrellis.raster._
import geotrellis.raster.geotiff.GeoTiffRasterSource
import geotrellis.raster.io.geotiff.GeoTiff
import workshop.data.TerrainTiles

TerrainTiles.getRasterSource(SpatialKey(100,100))

// There is an image on the internet, lets read it using RasterSource
// `RasterSource` interface is fundamental raster input interface in GeoTrellis.
// It abstracts over, file format, storage method and read implementation.
val source = GeoTiffRasterSource("https://geotrellis-demo.s3.amazonaws.com/data/aspect.tif")

// We can access basic raster metadata about our file
// Metadata will be read separately from pixels and user should expect it to be cached
source.extent
source.crs
source.cellType
source.dimensions

// RasterSource is a view into the raster, we have to explicitly read by default it reads all the pixels
val bands: Option[Raster[MultibandTile]] = source.read()

GeoTiff(bands.get, source.crs).write("/tmp/aspect.tif")

/* Window Reads:
Raster[MultibandTile]` can be read for any window described by `Extent` or pixel `GridBounds` intersecting the raster.
he extent of the resulting raster may not match the query extent but will always be aligned to pixel edges of the result raster.
When the query extent does not intersect the underlying raster `None` will be returned from the `read` methods.
 */
import geotrellis.vector.Extent
val bbox: Extent = source.extent.buffer(-source.extent.width / 4,  -source.extent.height /4)
val raster = source.read(bbox)
raster.get.extent
raster.get.dimensions

/* Reading Tiles:
Often rasters are too large to process at once and need to be tiled.
[LayoutTileSource] can be used to read tiles from any raster.
*/
val layout = LayoutDefinition(source.gridExtent, tileSize = 256)
val tiles = LayoutTileSource.spatial(source, layout)

import geotrellis.layer.SpatialKey

tiles.read(SpatialKey(0,0))
tiles.keys

/* Views:
RasterSource interface provides a way to perform a lazy resampling and reprojection
where the resulting `RasterSource` instance has the metadata of modified raster
but no raster transform is performed until a read request.
This feature is similar to GDAL VRT, indeed `GDALRasterSource` is backed by GDAL VRTs.
After transformation all read queries and all of the rater metadata is reported in target `CRS`. */
import geotrellis.proj4._

val rsWM = source.reproject(WebMercator)
rsWM.crs
rsWM.extent

// Lets fit our rasters to slippy map tiling scheme
val scheme = ZoomedLayoutScheme(WebMercator, 256)
val level = scheme.levelFor(rsWM.extent, rsWM.cellSize)
val tileSource = LayoutTileSource.spatial(
  source = source.reprojectToGrid(WebMercator, level.layout),
  layout = level.layout)

tileSource.keys