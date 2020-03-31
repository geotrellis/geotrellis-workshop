// # Layer Addressing

import geotrellis.layer.{LayoutDefinition, SpatialKey, ZoomedLayoutScheme}
import geotrellis.proj4.WebMercator
import geotrellis.raster.GridExtent
import geotrellis.vector.Extent

// TODO: insert picture of the tile vs extent with the key highlighted

SpatialKey(4,24)

// SpatialKeys refer to a tile in a [LayoutDefinition],
//  a regular grid of tiles covering a geographic area
val layout =
  LayoutDefinition(
    GridExtent(Extent(0, 0, 100, 100), cols = 100, rows = 100),
    tileCols = 10, tileRows = 10
  )

// Its up to the application to associate a key with the correct layout
//  SpatialKey(0,0) is in upper left (array convention)
//  Extent(0,0,1,1) is in the lower left (map convention)

SpatialKey(0, 9).extent(layout) // lower left
SpatialKey(0, 0).extent(layout) // upper left
SpatialKey(9, 0).extent(layout) // upper right
SpatialKey(9, 9).extent(layout) // lower right

// Layouts are often part of ia power of two pyramid (ex: Slippy Map)
val scheme = ZoomedLayoutScheme(WebMercator, tileSize = 256)
val zoom8 = scheme.levelForZoom(8).layout

// LayoutDefinition can translate between tile BBOX and CRS BBOX through mapTransform member
val ottawaWM = Extent(-8621691, 5604373, -8336168, 5805297)
val tileBounds = zoom8.mapTransform.extentToBounds(ottawaWM)
val tileExtent = zoom8.mapTransform.boundsToExtent(tileBounds)

// Note that this is snapping out extent to closest tile borders
tileExtent.contains(ottawaWM)
ottawaWM.contains(tileExtent)

// You can intersect complex geometries with layout to find intersecting tiles
zoom8.mapTransform.keysForGeometry(ottawaWM.toPolygon())

