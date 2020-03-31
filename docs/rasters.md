---
id: rasters
title: Rasters
sidebar_label: Rasters
---

In GeoTrellis, the GIS notion of a `Raster` is represented as a case class (basically, just a labelled tuple) with something implementing `CellGrid` (almost always [`Tile`](tiles.md)) on one side and an `Extent` on the other. The `CellGrid` represents data on the surface of a sphere while the `Extent` specifies how those values map onto said sphere (in an appropriate projection; see [map projections](projection.md) for more).

```scala mdoc:silent
import geotrellis.raster._
import geotrellis.vector.Extent
// The tile
val tile = ArrayTile(1 to 100 toArray, 10, 10)
// and its spatial extent
val extent = Extent(0, 0, 1, 1)
// jointly form a raster
val raster = Raster(tile, Extent(0, 0, 1, 1))
```

TODO: image/sketch/diagram of `Raster` components

A `Raster`, having both a grid of cells and a specification of the region that grid represents, is able to calculate the distance each cell in a `CellGrid` must cover to satisfy the width and height of its corresponding `Extent`. In practice this means that, when resampling or reprojecting, a `Tile` (which holds its data without regard to that data's extension in the real world) doesn't have enough information to get the job done; these cases generally demand a `Raster` or else require an `Extent` from which a `Raster` can be built.