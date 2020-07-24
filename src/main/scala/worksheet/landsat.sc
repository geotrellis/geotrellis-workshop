import geotrellis.raster.geotiff.GeoTiffRasterSource
import geotrellis.raster._
import workshop.Util

//def bandURI(band: String) = s"s3://geotrellis-workshop/landsat/LC81070352015218LGN00_$band.TIF"
def bandURI(band: String) = s"/Users/eugene/workshop-data/landsat/LC81070352015218LGN00_$band.TIF"
val greenBand = GeoTiffRasterSource(bandURI("B3"))
val redBand   = GeoTiffRasterSource(bandURI("B4"))
val nirBand   = GeoTiffRasterSource(bandURI("B5"))
val qaBand    = GeoTiffRasterSource(bandURI("BQA"))


// What is going on with CellType here?
val greenTile: Tile = ???
val qaTile: Tile = ???


// Mask clouds using QA band
val maskedTile =
  greenTile.combine(qaTile) { (v: Int, qa: Int) =>
    val isCloud = qa & 0x8000
    val isCirrus = qa & 0x2000
    if(isCloud > 0 || isCirrus > 0) { NODATA }
    else { v }
  }


// Make GREEN, RED, NIR tile
val multibandTile: MultibandTile = ???

// Using macro generated combine, very fast
val ndviTile =
  multibandTile.combineDouble(0, 1) { (r: Double, ir: Double) =>
    if (isData(r) && isData(ir))
      (ir - r) / (ir + r)
    else
      Double.NaN
  }

// Render PNG, why not?
val colorMap = workshop.Resource.Colors.ndvi
ndviTile.renderPng(colorMap)
