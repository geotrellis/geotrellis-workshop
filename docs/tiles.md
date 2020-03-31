---
id: tiles
title: Tiles
sidebar_label: Tiles
---

The `Tile` interface is the central representational structure for raster information.

Among the most common implementations of `Tile` is the `ArrayTile`, so named because it is backed by a normal JVM array. The examples in this document use an `ArrayTile` but should be illustrative of the `Tile` API more generally. In addition to the array of raw data, an `ArrayTile` must know how many columns and rows there are so that it can properly interpret (x, y) indices as positions within its one-dimensional, backing array.

### Creating a Tile
A 10x10 `Tile` backed by an array of `Int`s, is created to be used in the examples below:
```scala mdoc:silent
import geotrellis.raster._
val rawData = 1 to 100 toArray
val cols = 10
val rows = 10
val tile100 = ArrayTile(rawData, cols, rows)
```

### Tile Proportions
As mentioned above, each `Tile` must track its own shape. This information is available on the tile instance:
```scala mdoc
tile100.cols
tile100.rows
tile100.dimensions
```

### Data Types
The data type of tiles is represented by a `CellType`. Each `CellType` defines
1. Bit Width - how much memory per cell of data?
2. Floating Point? (predicate) - is each cell floating point or integral?
3. NoData Value - the sentinel value or lack thereof which signifies a lack of data and which should not be used most calculations

Accessing a tile's `CellType` relatively simple:
```scala mdoc
tile100.cellType
```

If a `Tile`'s `CellType` is incorrect, there are a couple of options for resolving the matter.
```scala mdoc:silent
// Here's an integer celltype with a 'NoData' value of 55
val ct = IntUserDefinedNoDataCellType(55)
// If the underlying data is correct but the celltype is mislabelled:
val interpreted = tile100.interpretAs(ct)

// If the underlying data should be converted to a new celltype
val converted = tile100.convert(ct)
```

```scala mdoc
interpreted.cellType
converted.cellType
```


### Accessing Tile values
Retrieval of values from within a tile is relatively simple though performance considerations on the JVM introduce some complexity. To avoid potential slowdowns in code that iterates through a `Tile` - potentially retrieving thousands or tens of thousands of values - GeoTrellis had to keep the methods for returning `Int` (integral values) and `Double` (floating point values) separate. The naming convention used throughout GeoTrellis is `<func>` for the `Int` case and `<func>Double` for `Double`s

#### Individual Values
Accessing a single value with `get`
```scala mdoc
tile100.get(5, 5)
tile100.getDouble(5, 5)
```

#### Iterating Through Cell Values
Applying a side-effecting function to each cell's value:
```scala mdoc:silent
val feInt = { i: Int => println(i) }
val feDbl = { i: Double => println(i) }
tile100.foreach(feInt)
tile100.foreachDouble(feDbl)
```

Transform a tile with any `Int => Int` or `Double => Double` function:
```scala mdoc:silent
val mapInt = { i: Int => i + 1 }
val mapDbl = { i: Double => i + 1 }
tile100.map(mapInt)
tile100.mapDouble(mapDbl)
```