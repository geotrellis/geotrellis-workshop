---
id: rastersource
title: RasterSource
sidebar_label: RasterSource
---

`RasterSource` interface is fundamental raster input interface in GeoTrellis.
It abstracts over file format, access schema, and reader implementation.

As of GeoTrellis 3.0 implementations exist for:
- `GeoTiffRasterSource`: GeoTrellis GeoTiff Reader
- `GeoTrellisRasterSource`: GeoTrellis Indexed Layers
- `GDALRasterSource`: GDAL through [`gdal-warp-bindings`](https://github.com/geotrellis/gdal-warp-bindings/)

## Metadata

`RasterSource`s are lazy, performing minimum I/O on instantiation. Raster metadata is read on demand and implementations are expected to cache it for repeated access through available fields.

```scala mdoc
import geotrellis.raster.geotiff.GeoTiffRasterSource

val source = GeoTiffRasterSource("https://geotrellis-demo.s3.amazonaws.com/data/aspect.tif")

source.extent
source.crs
source.cellType
source.dimensions
source.metadata.attributes
```

## Windows

GeoTrellis is designed around the assumption that rasters may be large and need to be split for parallel processed. Therefore the primary way to read with `RasterSource` is through windows into the source raster. When the file format supports it, like Cloud Optimized GeoTiff, these reads can be very efficient.

- Result of a window read is an `Option[Raster[MultibandTile]]`
- When the query extent does not intersect the source raster `None` will be returned
- Reads will be snapped to the pixel grid of the source raster, that may differ from the query extent
- Reads are not buffered when query extent partially covers the source raster

```scala mdoc
import geotrellis.raster._

val bbox = source.extent.buffer(-source.extent.width/4,  -source.extent.height/4)
val centerWindow = source.read(bbox)
centerWindow.get.dimensions

val leftCorner = source.read(GridBounds(0L, 0L, 255L, 255L))
leftCorner.get.dimensions
```

Additional read methods `readExtents` and `readBounds` are provided for reading multiple windows at once. This allows the `RasterSource` implementation to perform optimizations for the read sequence. For instance `GeoTiffRasterSource` ensures that each GeoTiff segment is read only once, even if it contributes to multiple windows. This optimization is not guaranteed for all implementations of `RasterSource`.

## Views

`RasterSource` provides a way to perform a lazy resampling and reprojection.
These operations produce a new `RasterSource` instance which is a lazy view of the source raster.
Both metadata and results of read function are consistent with this the view.
This feature is similar to GDAL VRT, indeed `GDALRasterSource` is backed by GDAL VRT.

```scala mdoc
import geotrellis.proj4._
import geotrellis.vector._
import geotrellis.raster.resample.{ NearestNeighbor }

val wgs84Source = source.reproject(LatLng, method = NearestNeighbor)
wgs84Source.extent

val w1 = wgs84Source.read(Extent(-78.68, 35.76, -78.65, 35.78))

w1.get.dimensions
```

Often the view may need to be matched to a specific resolution and pixel offset.
This is a requirement to be able to combine two rasters in a map algebra operation.

```scala mdoc
val minuteGrid = GridExtent(Extent(-180, -90, 180, 90), cols = 360*60, rows = 180*60)

val w2 = source.reprojectToGrid(LatLng, minuteGrid.toGridType[Long], NearestNeighbor)

w2.dimensions
```

## Read Tiles

When a raster needs to be read by tile keys from a known layout `LayoutTileSource` can be used.
`LayoutTileSource` imposes tile layout of `LayoutDefinition` (see [layer model](layer-model.md)) on an instance of `RasterSource`.

Lets read our raster as a web tiler would, using tile grid for TMS zoom level 14:

```scala mdoc
import geotrellis.layer._

val scheme = ZoomedLayoutScheme(WebMercator, 256)
val level = scheme.levelForZoom(14)

val tileSource = LayoutTileSource.spatial(
  source = source.reprojectToGrid(WebMercator, level.layout),
  layout = level.layout)

tileSource.keys.take(3)

tileSource.read(SpatialKey(4609, 6448))
```
