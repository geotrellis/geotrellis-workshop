---
id: rendering-images
title: Rendering Images
sidebar_label: Rendering Images
---

In many cases, the point of working with rasters is rendering images which facilitate quick interpretation and use by human analysts. This process of rendering imagery from rasters is a question of mapping the contents from each cell of data to a color which represents those contents. To this end, `ColorMap`, `ColorRamp`, and some RGB-handling conveniences are provided in the `geotrellis.raster.render` package and discussed (with examples) below.

> The tools provided by GeoTrellis core are relatively low level and will be the focus of what follows. We'll be looking at how to specify desired colors for avoiding most discussion about which colors are desirable/sensible.

## Representing RGB values

RGB values exist in a very simple cubic 'color-space'. A three-dimensional space, referencing any point (which is a color) can be done with three numbers. Those numbers represent R, G, and B values the combination of which results in a full-fledged RGB color. This way of referring to colors is even familiar to non-technical people who often don't even realize it because of its widespread adoption for internet standards.

Let's take a look at the color red. Here's its representation in the hexadecimal notation that people are often familiar with: `#FF0000`. If be break this number apart into groups of two decimal places, the components are easier to see:
R: FF = 255
G: 00 = 0
B: 00 = 0

```scala mdoc
0xFF0000 // another way to say #FF0000
```

![RGB Color Cube](https://upload.wikimedia.org/wikipedia/commons/0/05/RGB_Cube_Show_lowgamma_cutout_a.png "RGB Color Space")

Because 0 to 255 is the size of a byte and because integers are 4 bytes, integers are another way of naming RGB colors + an alpha channel value. Integers are a bit more difficult to read as colors than their hex-formatted alternative so GeoTrellis introduces a few conveniences. You can explicitly and separately specify R, G, B, and A or else directly use a hex code in the `0x` format rather than the more familiar `#` format

```scala mdoc
import geotrellis.raster.render.RGBA
RGBA(255, 0, 0, 0) == RGBA(0xFF000000).int
RGBA(255, 0, 0, 0).red == RGBA(0xFF000000).red
RGBA(255, 0, 0, 0).green == RGBA(0xFF000000).green
RGBA(255, 0, 0, 0).blue == RGBA(0xFF000000).blue
RGBA(255, 0, 0, 0).alpha == RGBA(0xFF000000).alpha
```

## An Example Tile

Before we look at the rendering options, it would be helpful to create a simple, predictable tile to render. We'll use this tile to illustrate the rendering API. This tile will range from 1 to 100 with 1 in the top left and 100 in the bottom right.

> GeoTrellis tiles are indexed in a row-major order such that the first row contains values 1 through 10; the second, 11 through 20; the third, 21 through 30; etc

```scala mdoc:silent
import geotrellis.raster._
val tile = ArrayTile(1 to 100 toArray, 10, 10)
```

## ColorMap

A `ColorMap` defines 
1. a set of associations between break points (which are expected cell values) and colors (integers representing RGB color values)
2. the necessary set of options for determining on which side of the break its associated color should be applied. (Is the color applied to values greater than, less than, or equal to the provided break?)

This `ColorMap` will render only values 42 (as red) and 99 (as green):
```scala mdoc
import geotrellis.raster.render.Exact
val red = RGBA(255, 0, 0, 255)
val green = RGBA(0, 255, 0, 255)
val options = ColorMap.Options.DEFAULT.copy(classBoundaryType=Exact)
val colorMap = ColorMap(Map(42 -> red, 99 -> green), options)
```

Using this `ColorMap` to render a PNG or JPG is easy:
```scala mdoc:silent
tile.renderPng(colorMap)
tile.renderJpg(colorMap)
```

## ColorRamp

A `ColorRamp` specifies only a set of colors.
```scala mdoc
val colorRamp = ColorRamp(0xFF0000, 0x00FF00, 0x0000FF)
```

### Generating a ColorMap
Rendering with a `ColorRamp` requires first turning it into a `ColorMap` so that those colors are associated with cell values. Despite the extra step, they tend to be easier to work with than `ColorMap`s because this delayed pairing provides some flexibility. A `ColorRamp` has two main ways of producing a `ColorMap`. First, it can use a `Tile`'s [`Histogram`](histograms.md). Second, breaks can be provided manually.

#### Via Histogram
A histogram for a given distribution allows us to determine where in that distribution a new value would fall. They also allow us to approximate the value which *would* fall at arbitrary percentiles. This can be used to generate breaks which favor areas of greater density within your data's distribution.
```scala mdoc
val histogram = tile.histogram
val generatedCMap = colorRamp.toColorMap(histogram)
```


The `Histogram`-based `toColorMap` method will only generate as many breaks as there are colors in your ramp. Given a `ColorRamp` of 3 colors and a tile ranging from 1 to 100 (this makes the math simple) we should expect to see a `ColorMap` with breaks of 33, 67, and 100.

> Take care that your `ColorRamp` has enough colors in it when using the `Histogram`-based `toColorMap` method!
```scala mdoc
generatedCMap.breaksString
```

If more breaks are required, first take advantage of `ColorRamp`s ability to generate a larger `ColorRamp` through interpolation and then generate your `ColorMap`:
```scala mdoc
val largeColorRamp = colorRamp.stops(10)
val largeGeneratedCMap = largeColorRamp.toColorMap(histogram)
largeGeneratedCMap.breaksString
```

#### Via Breaks
If certain values are meaningful and well known ahead of analyzing data, they can be used along with `ColorRamp` to generate a `ColorMap` with as many colors as provided breaks.
```scala mdoc
val ndviColors = ColorRamp(0xFF0000, 0xFFFF00, 0x00FF00)
val ndviValues = Array(-1, -0.5, 0, 0.5, 1)
ndviColors.toColorMap(ndviValues).breaksString
```

## Exporting Rendered Imagery
Rendered images can be conveniently written to disk or serialized as an array of bytes to be stored or exported.

Writing to disk:
```scala
tile.renderJpg(colorMap).write("/tmp/rendered.jpg")
```

Serializing to an array of bytes:
```scala mdoc:silent
tile.renderJpg(colorMap).bytes
```