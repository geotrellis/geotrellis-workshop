package workshop

import geotrellis.raster._

object Calculations {

  /** Calculates the normalized difference vegetation index
   *
   * @param r  value of red band
   * @param ir value of infra-red band
   */
  def ndvi(r: Double, ir: Double): Double =
    if (isData(r) && isData(ir))
      (ir - r) / (ir + r)
    else
      Double.NaN

  /** Calculates the normalized difference water index
   *
   * @param g  value of green band
   * @param ir value of infra-red band
   */
  def ndwi(g: Double, ir: Double): Double =
    if (isData(g) && isData(ir))
      (g - ir) / (g + ir)
    else
      Double.NaN

  /** This function will set anything that is potentially a cloud to NODATA
   *
   * @param tile sensor band tile
   * @param qaTile Landsat QA mask tile
   */
  def maskClouds(tile: Tile, qaTile: Tile): Tile =
    tile.combine(qaTile) { (v: Int, qa: Int) =>
      val isCloud = qa & 0x8000
      val isCirrus = qa & 0x2000
      if(isCloud > 0 || isCirrus > 0) { NODATA }
      else { v }
    }
}