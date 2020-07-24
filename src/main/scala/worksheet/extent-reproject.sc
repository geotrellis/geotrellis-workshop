import geotrellis.vector._
import org.locationtech.jts.densify.Densifier
import geotrellis.proj4._

// Extent covers most of USA
val extent = Extent(-107.15975813,28.08543124,-85.16727532,48.20024378)

// Reproject to EPSG:5070, corners ONLY
// Densify
// Reproject back as Polygon
// Read as GeoJSON
