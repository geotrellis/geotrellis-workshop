---
id: histograms 
title: Histograms 
sidebar_label: Histograms 
---

Histograms efficiently compute statistics such as min, max, median, mode and median. They can also derive quantile breaks, commonly used to generate color maps as described in [Rendering Images](rendering-images.md#via-histogram).

It’s often useful to derive a histogram from rasters, which represent a distribution of the pixel values. In GeoTrellis, we support two types of histograms:

- `Histogram[Int]`: represents the exact counts of integer values
- `Histogram[Double]`: represents a grouping of values into a discrete number of buckets

These types are in the `geotrellis.raster.histogram` package.

The default implementation of `Histogram[Int]` is the `geotrellis.raster.histogram.FastMapHistogram`, developed by Erik Osheim, which utilizes a growable array structure for keeping track of the values and counts.

The default implementation of `Histogram[Double]` is the `geotrellis.raster.histogram.StreamingHistogram`, developed by James McClain and based on the paper:

> Ben-Haim, Yael, and Elad Tom-Tov. "A streaming parallel decision tree algorithm."  The Journal of Machine Learning Research 11 (2010): 849-872.

## The Basics

GeoTrellis `Tile` instances have `histogram` and `histogramDouble` properties that return `FastMapHistogram` and `StreamingHistogram` respectively.

```scala mdoc
import geotrellis.raster._

val rs = RasterSource("s3://geotrellis-demo/cogs/harrisburg-pa/elevation.tif")
val elevationRaster = rs.read.get
val elevationHistogram = elevationRaster.tile.band(0).histogram
elevationHistogram.statistics
```

For small rasters that fit into memory, these properties provide a quick look at summary raster statistics.

## Polygonal Summary

A common operation to perform on rasters is a polygonal summary, where a mathematical operation is computed across each pixel within a geometric boundary that intersects the raster bounds. GeoTrellis supports computing histograms via it's polygonal summary operation. First we'll need the following imports:

```scala mdoc
import geotrellis.proj4.LatLng
import geotrellis.raster.summary.polygonal._
import geotrellis.raster.summary.polygonal.visitors.FastMapHistogramVisitor
import geotrellis.vector._
```

Let's assume we want to compute an elevation summary for an area around Carlisle, PA. This area is entirely covered by our raster, but it need not be. Let's construct our geometry and reproject it to the CRS of our RasterSource.

```scala mdoc
// Carlisle, PA
val geom = Polygon(
  LineString(
    List(
      (-77.25414276123047, 40.168642443446366),
      (-77.1188735961914, 40.168642443446366),
      (-77.1188735961914, 40.24939807617368),
      (-77.25414276123047, 40.24939807617368),
      (-77.25414276123047, 40.168642443446366)
    )
  )
)
val geomInRsCrs = geom.reproject(LatLng, rs.crs)
```

To actually compute the histogram, we need to call the polygonal summary operation and then extract the histogram from the PolygonalSummaryResult ADT.

```scala mdoc:silent
val raster = Raster(elevationRaster.tile.band(0), rs.extent)
```

```scala mdoc
val result = raster.polygonalSummary(geomInRsCrs, FastMapHistogramVisitor)
val histogram = result.toOption.get
histogram.statistics
```
