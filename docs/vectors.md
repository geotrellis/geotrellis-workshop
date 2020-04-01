---
id: vectors
title: Vectors
sidebar_label: Vectors
---

The [Java Topology Suite](https://github.com/locationtech/jts) has been an important and influential library in GIS vector processing.
It is something of a standard on the JVM and has even been ported to C as [GEOS](https://www.osgeo.org/projects/geos/).
Instead of reinventing the wheel, GeoTrellis uses JTS as the foundation of its vector module - any GeoTrellis `Geometry` is really a JTS `Geometry` underneath.

## Construction

To get a sense for why GeoTrellis vector has introduced geometry utilities, compare this example of creating a `Point` with the standard JTS API to the GeoTrellis code below it which creates the same type.

```scala mdoc
// with JTS
import org.locationtech.jts
val gFactory = new jts.geom.GeometryFactory()
val coord = new jts.geom.Coordinate(0, 0)
val jtsPoint = gFactory.createPoint(coord)
```

```scala mdoc
// with GeoTrellis
import geotrellis.vector._
val point = Point(0, 0)
```

### Managing types

```scala mdoc
point
point.geom
```
The values returned appear to be the same (`POINT (0 0)`).
But take note of the types and things seem to have come apart: we get `Point` and `Geometry`.
In general when writing Scala, but especially important when working with the `geotrellis.vector` package.
Remember to always pay attention to the types!

The `.geom` method allows specific vector types (e.g. `Point`, `Line`, `Polygon`) to be upcast to their `Geometry` supertype.
Going the other direction (e.g. `Geometry` => `Point`) is slightly more involved and looks quite a bit like downcasting in scala generally.
Examples of downcasting are provided below and listed from least to most safe.

```scala mdoc
// Erasing type specificity (Point => Geometry)
val theGeom: Geometry = point.geom

// Dangerous! (runtime errors)
theGeom.asInstanceOf[Point]

// Safe-ish (custom error thrown)
theGeom match {
  case p: Point => p
  case _ =>
    throw new java.lang.IllegalStateException("This can only be a point")
}

// Safer (can't throw) + More Idiomatic
theGeom match {
  case p: Point => Some(p)
  case _ => None
}
```

### Advanced Construction Options

JTS, like many Java libraries, requires a bit more ceremony to begin creating objects than is typical in Scala.
Configuration details which are handled by JTS through parameterization of the `org.locationtech.jts.geom.GeometryFactory` have been given a sensible defaults which are automatically leveraged by geometry creation helper functions.
These defaults can be changed by providing a configuration file to override the [default configuration](https://github.com/locationtech/geotrellis/blob/master/vector/src/main/resources/reference.conf).
Settings are picked up when the application starts and should transparently modify geometry creation.


## Spatial Methods

### Spatial Predicates

All [DE-9IM](https://en.wikipedia.org/wiki/DE-9IM) spatial predicates are provided by JTS as methods on `Geometry`.

```scala mdoc
val line = LineString(Point(-1, -1), Point(1, 1))
line.covers(line)
```

In Scala, language features allow us to write methods as infix operators which makes the JTS API even more readable
```scala mdoc
point covers point
```

### Set Operators: Union, Difference, Intersection

Unlike predicates, which always return `true` or `false` regardless of input, `union`, `difference`, and `intersection` result types vary with their inputs.
To model the complexity of these results, GeoTrellis provides the `GeometryResult` and subtypes which helps the compiler know which cases need to be handled.
Users may choose between the normal JTS API or the more type-literate `geotrellis.vector` API.

#### Weakly Typed (JTS API)

For JTS, the result type of any `union`, `difference`, or `intersection` will simply be a `Geometry`.

```scala mdoc
point intersection line
point union line
point difference line
```

#### Typed API (GT API)

Using the more heavily typed API, `intersection` is `&`; `union` is `|`; and difference is `-`.
Instead of Geometries, these methods return `GeometryResult`s which allow the compiler to know a little bit more about what kinds of result are even possible.
There are too many `GeometryResult` classes to list them all.
As an example, the type `PointOrNoResult` is returned whenever a set operator applied to two `Geometry`s can only return a `Point` or `NoResult` (in case it is empty).

```scala mdoc
point & line
point | line
point - line
```

> In the above code, note that the type looks a bit different than the value (`PointOrNoResult` doesn't appear to be the same as `PointResult`).
This is because the compiler can only know that the result is *some* `PointOrNoResult`
The specific underlying value of said type (`Point` or `NoResult`) can't be determined until runtime.

## Projected Geometry

Vectors do not, themselves, keep track of the projection in which their units are correctly defined.
This means that it is up to users to keep track of projections and to ensure that comparisons are not naively carried out between vectors whose points' coordinates are in different projections.

Below, we've got a latitude/longitude projected `Point` (defined in terms of arcseconds) and want to determine its location under the Web Mercator projection (defined in terms of meters).

```scala mdoc
import geotrellis.proj4.{LatLng, WebMercator}
val llPoint = Point(80, 80)
val wmPoint = llPoint.reproject(LatLng, WebMercator)
```
For more, refer to the section on [map projections](projection.md)

## Features

Vectors are usually not consumed by themselves.
Instead, they will generally label otherwise interesting tracts and include properties which correspond to said tracts.
Consider the case of US Census data: Census Block polygons have attached racial and economic breakdowns of the regions they correspond to.
Such shape + property pairings are known as features and represented within GeoTrellis as `geotrellis.vector.Feature`s.

### Feature Creation

At the lowest level, `Feature` is little more than a tuple of some `G` such that `G` is a `Geometry` (or `G <: Geometry` as it is written in Scala) and any type `D`.

```scala mdoc
Feature[Point, String](point, "extra data")
```

There also exist helper constructors which fix `G` while allowing `D` to vary:

```scala mdoc
PointFeature[String](point, "point with string")
LineStringFeature[String](line, "just a line with a string")
```

### Features and JSON

The most common GIS application is probably the 'dots on a map' representation of locations as markers pinned into appropriate locations.
Polygon choropleths are likely a close second.
Both of these cases depend on reading spatial data and its non-spatial correlates to determine what information should be displayed.
This is why `Feature` exists.

Due to the regular need to ship features from database to client or from client to server, the standards for sending and receiving geometries are widely adopted and well documented.
Chief among the standards developed for exactly this need is [GeoJson](https://geojson.org/)

> Circe encoders/decoders have been provided for many of the types within GeoTrellis.
Before you can use these, however, `io.circe.syntax._` *must* be imported to pick up the necessary implicits.

```scala mdoc
import _root_.io.circe.syntax._
Point(12, 34).asJson.noSpaces
```

#### Feature Serialization

To serialize `Feature` records into GeoJson, it is necessary to implement a Circe `Encoder` for data type `D` (not the geometry, `G`).

> Circe is a great library for managing serde of JSON data and a great example of designing types to assist the compiler in reporting bugs before they happen.
As nice as all that is, Circe is primarily a JSON library and is focused on avoiding invalid JSON.
GeoJson, as a subset of JSON, isn't necessarily valid for any given instance of JSON.
As a result, it is technically possible to produce an invalid `properties` field on the `Feature`'s serialized output (properties must be an object to be valid GeoJson, a `String` alone won't cut it):

```scala mdoc
Feature(Point(12, 34), "stringTest").asJson.noSpaces
```

The easiest way to construct valid GeoJson is to construct a case class which has all the properties to be stored in a `Feature`.
Below, a simple case class, `PropertyData`, is used and an encoder/decoder pair is generated for this class.
Output JSON will keep properties in the context of a javascript object `{}`

```scala mdoc
import _root_.io.circe._
import _root_.io.circe.generic.semiauto._
case class PropertyData(str: String)
implicit val encoder: Encoder[PropertyData] = deriveEncoder[PropertyData]

val featureString =
  Feature(Point(12, 34), PropertyData("stringTest")).asJson.noSpaces
featureString
```

#### Feature Deserialization

Deserialization demands that we have a Circe `Decoder` rather than an `Encoder`.
The other added complexity is that we must extract results from an `Either` which will contain (`Right` side) success or else (`Left` side) a description of what went wrong.

```scala mdoc
import _root_.io.circe.parser.decode
implicit val decoder: Decoder[PropertyData] =
  deriveDecoder[PropertyData]

// getting data out of the Either
decode[Feature[Point, PropertyData]](featureString) match {
  case Right(feature) => // Success
    feature
  case Left(exception) => // Failure + error msg
    throw exception
}
```
