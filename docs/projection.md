---
id: projection
title: Projection
sidebar_label: Projection
---

GoeTrellis uses [Proj4J](http://github.com/locationtech/proj4j) library to work with coordinate reference systems. In practice Proj4J classes are hidden behind the `geotrellis.proj4.CRS` trait which provides Scala API into the library.

```scala mdoc
import geotrellis.proj4._

val wgs84 = LatLng
val utm15N = CRS.fromEpsgCode(2027)
val southPA = CRS.fromString("+proj=lcc +lat_1=40.96666666666667 +lat_2=39.93333333333333 +lat_0=39.33333333333334 +lon_0=-77.75 +x_0=600000 +y_0=0 +ellps=GRS80 +datum=NAD83 +to_meter=0.3048006096012192 +no_defs")
```

## Reproject

GeoTrellis adds `reproject` methods to geometry and raster instances through implicit methods extension which allow their transformation between two coordinate reference systems.

```scala mdoc
import geotrellis.vector._
import geotrellis.raster._

val philly = Point(-75.1652, 39.9526)

philly.reproject(LatLng, southPA)

val raster = Raster(
  tile = ArrayTile.empty(DoubleCellType, 2, 2),
  extent = philly.buffer(0.01).extent)

raster.reproject(LatLng, southPA)
```

Note that neither `Geometry` nor `Raster` actually carry associated projection with them. It is up to the developer and the application to ensure that the reprojection being applied is consistent with the source data. Commonly small applications or batch jobs will know the source projection of the data during development. However, its a common source of perplexing data errors.

When building more complicated applications it may be helpful to associate the projection information with the rasters data using `ProjectedRaster` class.

```scala mdoc:silent
ProjectedRaster(raster, LatLng)
```