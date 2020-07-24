import geotrellis.raster._

val tile = ArrayTile(1 to 100 toArray, 10, 10)
println(tile.asciiDraw())