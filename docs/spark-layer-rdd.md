---
id: spark-layer-rdd
title: Tile Layer RDD
sidebar_label: Tile Layer RDD
---

![GeoTrellis Layer](assets/spark-tile-layer-partitions.png)

A layer is an `RDD[(SpatialKey, Tile)] with Metadata[TileLayerMetadata[SpatialKey]]`.
Conceptually a layer is a tiled raster. Functionally you can treat it as a distributed Map object.

- `SpatialKey` is from [`LayoutDefinition`](layer-model.md) defined by layer metadata
- Every `Tile` in a layer has same:
  - Dimensions (col x row)
  - Band Count
  - CellType
  - Map Projection
  - Spatial Resolution (pixels per map X/Y units)

The design philosophy behind GeoTrellis spark API is that it should work as much as possible with native spark types and should minimize number of new classes of wrappers. Therefore `TileLayerRDD` is first and foremost an `RDD` and has all of the methods available to RDD of its type. The spatial metadata is "extra".

**Note**:
There is no requirement that each key value occurs only once within a layer.
Typically that is the case, but ultimately the property can only be inferred based on the operations that that produced the layer.

## Pair RDD methods

Because each record in a layer RDD is a tuple key and value Spark provides additional methods through [`PairRDDFunctions`](https://github.com/apache/spark/blob/master/core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala) implicit [method extensions](https://github.com/apache/spark/blob//core/src/main/scala/org/apache/spark/rdd/RDD.scala#L2014-L2017):

- [`reduceByKey`](https://github.com/apache/spark/blob/0d997e5156a751c99cd6f8be1528ed088a585d1f/core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala#L306-L313)
- [`combineByKey`](https://github.com/apache/spark/blob/0d997e5156a751c99cd6f8be1528ed088a585d1f/core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala#L120-L133)
- [`groupByKey`](https://github.com/apache/spark/blob/0d997e5156a751c99cd6f8be1528ed088a585d1f/core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala#L506-L520)
- [`partitionBy`](https://github.com/apache/spark/blob/0d997e5156a751c99cd6f8be1528ed088a585d1f/core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala#L522-L534)
- [`join`](https://github.com/apache/spark/blob/0d997e5156a751c99cd6f8be1528ed088a585d1f/core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala#L536-L545)
- [`leftOuterJoin`](https://github.com/apache/spark/blob/0d997e5156a751c99cd6f8be1528ed088a585d1f/core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala#L547-L563)
- [`rightOuterJoin`](https://github.com/apache/spark/blob/0d997e5156a751c99cd6f8be1528ed088a585d1f/core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala#L565-L580)
- [`fullOuterJoin`](https://github.com/apache/spark/blob/0d997e5156a751c99cd6f8be1528ed088a585d1f/core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala#L713-L725)
- [`mapValues`](https://github.com/apache/spark/blob/0d997e5156a751c99cd6f8be1528ed088a585d1f/core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala#L744-L753)
- [`flatMapValues`](https://github.com/apache/spark/blob/0d997e5156a751c99cd6f8be1528ed088a585d1f/core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala#L755-L766)

Once a layer exists much of the work transforming a layer can be done these built-in Spark functions.

## Reading Layer

```scala
import org.apache.spark._

implicit val sparkContext =
  SparkContext.getOrCreate(
    new SparkConf(loadDefaults = true)
      .setMaster("local[*]")
      .setAppName("Demo")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryo.registrator", "geotrellis.spark.store.kryo.KryoRegistrator"))
```

Lets build a layer from a small demo file, we will have more later.
First we construct our `RasterSource` instances.

```scala
import geotrellis.layer._
import geotrellis.spark._
import geotrellis.raster.geotiff.GeoTiffRasterSource

val uri = "s3://geotrellis-demo/cogs/harrisburg-pa/elevation.tif"
val source = GeoTiffRasterSource(uri)
```

At this point we can read the file but we have to make a choice about what kind of tiling layout we want to use.
If we know ahead of time, we can skip this step. But if the input is more variable we can use `RasterSummary` object.
`RasterSummary` will expand the extent to cover all of the seen rasters, at highest resolution.

> `RasterSummary` will through an exception if it encounters sources in different projects because their extents are not comparable. If that's your problem, reproject your sources to common CRS before collecting the summary.

```scala
val summary = RasterSummary.fromSeq(List(source))
```

An instance of `RasterSummary` will produce for you a `LayoutDefinition` that covers the input data and matches your layout scheme. In this case lets snap our raster to one of the slippy zoom levels. This will up-sample our raster to snap it to closest zoom level

```scala
import geotrellis.spark._
import geotrellis.proj4.WebMercator

val layout = summary.layoutDefinition(ZoomedLayoutScheme(WebMercator, 256))
val layer = RasterSourceRDD.spatial(source, layout)

```

## Layer Metadata
// problem ... I can't run the code
```scala
val layer: TileLayerRDD[SpatialKey] = ???

layer.metadata

```



TODO: example of joining layers

## Partitioning

A layer does not restrict how its records are partitioned. Generally a [`HashPartitioner`](https://github.com/apache/spark/blob/v2.4.5/core/src/main/scala/org/apache/spark/Partitioner.scala#L104-L130) is used and records are distributed across all the partitions based on the modulo division of the key hash.

Specifying layer partitioning provides an interesting avenue for optimization. When joining two layers that have different partitioning a shuffle has to happen in order to co-locate all the records from layer A with all the records from layer B. However, if two layers share the same partitioner Spark can infer that all keys that could join to a given partition in layer A must come from a single partition in layer B. Such a join does not result in a shuffle and is thus an order of a magnitude faster.

```scala
sparkContext.stop()
```