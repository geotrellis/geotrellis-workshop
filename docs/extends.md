---
id: extents
title: Extents
sidebar_label: Extents
---

The GeoTrellis raster module depends on the [vector module](vectors.md) to handle the job of describing the scope of real territory (usually in projected units) over which a `Tile` is representative.
`Extent` is the class which, paired with a `Tile`, allows us to build a `Raster`.

Its constructor takes a minimum X, minimum Y, maximum X, and a maximum Y.
With these boundaries we can actually fix the corners of an associated `Tile` and use interpolation to determine mappings from real world coordinates within the provided `Extent` to values derived from that `Tile`'s cells.

```scala mdoc
import geotrellis.vector._
val extent = Extent(0, 0, 90, 90)
```

As discussed in [vectors](vectors.md), rather than reinvent the wheel, `geotrellis.vector` provides conveniences which leverage the great work done in JTS.
That fact is apparent here as well: the GeoTrellis `Extent` is the JTS `Envelope`.

```scala mdoc
extent.jtsEnvelope
```

## Extent Reprojection

As implied by the limited input to its constructor, an `Extent`can only represent a rectangular region which is aligned with the coordinate system.
GeoTrellis provides three different ways to reproject `Extents`

### Naive Reprojection (not recommended)

Naive reprojection of an `Extent` is very simple but can mislead.
As seen in the case of reprojection as a `Polygon`, `Extent` shapes can be expected to warp during projection.
Imagine turning a rectangle on its diagonal: drawing a rectangle which surrounds this diagonal rectangle but which remains parallel with the original ensures a larger rectangle than the one we turned on its diagonal.

```scala mdoc
import geotrellis.proj4.{LatLng, WebMercator, Transform}
val transform = Transform(LatLng, WebMercator)
extent.reproject(transform)
```

> Caution is warranted when naively reprojecting an `Extent`.
It can be heuristically useful, but should be relied upon for high precision calculations.
Instead, use `reprojectAsPolygon` if reprojecting only the `Extent` and cellgrid-aware reprojection if attempting to reproject a gridded space.

### Polygon Reprojection (recommended)

Rectangular regions become curvilinear regions after geodetic projection.
To see exactly which bits of territory in projection B are covered by an `Extent` defined for projection A, it should be reprojected as a polygon

```scala mdoc
extent.reprojectAsPolygon(transform, 0.01)
```

> This method adaptively refines its output.
To exit adaptive refinement, a tolerance value is required to define success.
In this case, 0.01 specifies the amount of deflection allowed in terms of distance from the original `Extent`'s line to the point which represents it under another projection

### CellGrid-Aware Reprojection (recommended)

Reprojection of a gridded space's extent imposes constraints not seen in the above cases.
In addition to the needing to accurately capture the transformation of an `Extent`, this method requires information about the grid of cells which corresponds to said input `Extent`.
This information is used to approximately preserve the pre-reprojection resolution.

They only information in addition to `Extent` required to compute this kind of reprojection is the resolution.
`Extent` plus resolution yields a `GridExtent` or a `RasterExtent`, either of which are suitable for carrying out reprojection of a gridded space.
Take note that the `CellSize` has been transformed in addition to the `Extent`:

```scala mdoc
import geotrellis.raster._
val cs = CellSize(1, 1)
val before = GridExtent[Long](extent, cs)
val after = before.reproject(LatLng, WebMercator)
```