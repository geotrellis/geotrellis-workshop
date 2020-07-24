package workshop

import java.io._
import java.net.{URI, URL}
import geotrellis.raster.render.ColorMap

object Resource {
  def apply(name: String): String = {
    val stream = getClass.getResourceAsStream(s"/$name")
    try scala.io.Source.fromInputStream(stream).getLines.mkString(" ") finally stream.close()
  }

  def lines(name: String): Seq[String] = {
    val stream = getClass.getResourceAsStream(s"/$name")
    try scala.io.Source.fromInputStream(stream).getLines.toList finally stream.close()
  }

  def url(name: String): URL = getClass.getResource(s"/$name")

  def uri(name: String): URI = getClass.getResource(s"/$name").toURI

  def path(name: String): String = getClass.getResource(s"/$name").getFile


  object Colors {
    val ndvi = ColorMap.fromString("0:ffffe5ff;0.1:f7fcb9ff;0.2:d9f0a3ff;0.3:addd8eff;0.4:78c679ff;0.5:41ab5dff;0.6:238443ff;0.7:006837ff;1:004529ff").get
  }
}