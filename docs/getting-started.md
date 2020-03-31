---
id: getting-started
title: Getting Started 
sidebar_label: Getting Started 
---

_GeoTrellis_ is a Scala library and framework that provides APIs for reading, writing and operating on geospatial raster and vector data. GeoTrellis also provides helpers for these same operations in Spark and for performing [MapAlgebra](https://en.wikipedia.org/wiki/Map_algebra) operations on rasters. It is released under the Apache 2 License.

There are a number of geotrellis subpackages available. We recommend getting started with `geotrellis-raster`:

```scala
libraryDependencies += "org.locationtech.geotrellis" %% "geotrellis-raster" % "@VERSION@"
```

Loading rasters via the `RasterSource` API is easy once you've got `geotrellis-raster` installed:

```scala mdoc
import geotrellis.raster._

val rs = RasterSource("gtiff+file://data/ls8_int32.tif")
rs.extent
```
