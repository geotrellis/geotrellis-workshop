import data.TerrainTiles
import geotrellis.vector._
import geotrellis.proj4._
import example._
import geotrellis.raster.gdal.GDALRasterSource
import geotrellis.raster.geotiff.GeoTiffRasterSource

val ottawaExtent = Extent(-77.44996800255721, 44.89096844334527, -74.88507125388062, 46.155460691100856)
val ottawa = Point(-75.70410292, 45.42042493)
val keys = TerrainTiles.layout.mapTransform.keysForGeometry(ottawaExtent.toPolygon())
val pols = keys.toList.map(_.extent(TerrainTiles.layout).toPolygon())

println(GeometryCollection(pols).toGeoJson())

// LC8 Scene over Ottawa
val lc8Uri = "https://landsat-pds.s3.amazonaws.com/c1/L8/015/028/LC08_L1TP_015028_20200119_20200128_01_T1/LC08_L1TP_015028_20200119_20200128_01_T1_B1.TIF"
val ots = GeoTiffRasterSource(lc8Uri)
val center = ots.extent.center.reproject(ots.crs, LatLng)


// Terrain Tile at center of LC8 scene
val key = TerrainTiles.layout.mapTransform.pointToKey(center)
// val source = GDALRasterSource("/vsigzip//vsis3/elevation-tiles-prod/v2/skadi/N46/N46W075.hgt.gz")
val source = TerrainTiles.getRasterSource(key)
val uri = TerrainTiles.tileUri(key)
key.extent(TerrainTiles.layout).toGeoJson()
source.extent.toGeoJson()

// - Plot footprints on geojson.io
// - co-register two rasters
// - Mask LC8 by mean value of elevation (hide all below the mean)
