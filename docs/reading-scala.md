---
id: reading-scala
title: Reading Scala
sidebar_label: Reading Scala
---

A joy/headache of writing Scala is just how many ways there are to write any given program.
On the one hand, it is nice to have some options.
On the other, it necessitates the adoption of a programming style that works well for you, your team, and your projects.
These conventions, as much about avoiding bugs as signaling intent to readers, are often expressed in terms of how close to purely functional vs how close to a Java/Object Oriented style they are.

Beyond these stylistic impacts, different library ecosystems will be a better fit for different programming styles.
GeoTrellis is closer to the functional side of things and has tended to prefer the adoption of technologies like Cats, Circie, and Doobie which are put out and supported by [TypeLevel](https://typelevel.org/)
These libraries tend to be on the functional side but unlike many FP ecosystems, they also tend to be geared towards building real applications and providing solid documentation.

## Functional Programming

So, what is functional programming?
Here's how it is (not exhaustively) described on [Wikipedia](https://en.wikipedia.org/w/index.php?title=Functional_programming&oldid=947768235):
FP is the paradigm that "treats computation as the evaluation of mathematical functions and avoids changing-state and mutable data. It is a declarative programming paradigm in that programming is done with expressions or declarations instead of statements. In functional code, the output value of a function depends only on its arguments, so calling a function with the same value for an argument always produces the same result."


### Prefer Immutability

Mutability refers to the propensity for a reference to change in value within the same scope over the course of a program's run.
If a variable is mutable, merely seeing its declaration isn't enough to infer its value.
The variable could have been updated or even reset to undefined and it often requires a great deal of effort to verify.
Debugging mutable code might require printing the value at multiple points during the program's execution and building a sophisticated model of the program in your head.

Take a look at this python code which attempts to calculate the average of a list and note that `hundredSum`, which keeps track of the sum as new numbers roll in, is reassigned:

```python
hundred = [x for x in range(100)]
hundredSum = 0
for num in hundred:
  hundredSum = hundredSum + num
mean = hundredSum/len(hundred)
```

In javascript, variables are often denoted with `var`.
Borrowing from and extending that convention, Scala allows immutable references by declaring them with `val` and mutable references if declared with `var`.
Remember, `val` is a value whereas `var` is variable (i.e. its value can vary)

```scala mdoc:silent
val lst = 1 to 100 toList
val mean = lst.sum / lst.length
```

> Scala is often less strict than other functional languages.
It is thus easier for us to write mutable code than it would be in some of the more restrictive functional languages.
This is a bit of a double-edged sword.
It is bad because it means that we can accidentally introduce code that is relatively likely to have/lead to bugs.
It is good because the machinery and processes which constitute a program's instantiation are more similar to iterating and updating than applying a function.
Abstraction has costs and Scala allows us the escape-hatch of using mutable code where such optimizations are necessary

### Prefer Referential Transparency

A function is said to possess referential transparency if that function's return value/output depends entirely on the input and if the same input value must produce the same output.
This property, much like immutability, makes code far easier to read.
To see why, consider a what would be required to implement a function which lacks referential transparency.
To arrive at different result values for a given 'function' given the exact same parameters, that function would have to effect (update or read) an external value.
Thus, we say that such functions have 'side-effects'.
Such functions can't be reasoned about on their own merits.
Instead, as with mutable code, understanding a function which has side-effects demands a sophisticated model of the program in which it exists.

Good - nothing outside the function is referred to.
This function will always work just as expected.
```scala mdoc
def plusOne(num: Int): Int = num + 1
```

Bad - this function works correctly only the first time that it is called
```scala mdoc:reset
var one = 1
def plusOne(num: Int) = {
  val toReturn = num + one // side-effect (reading function-external value)
  one = 2 // side-effect (reassignment)
  toReturn
}
```

## Leverage Higher Order Functions

Higher order functions are functions which, themselves, take functions as arguments.
They're popular even outside of functional programming languages at this point and they can dramatically simplify certain compound problems.

To see how higher order functions can simplify the representation of complex processes, consider the case of the javascript callback.
I send out a request and expect to get a response back within a few hundred milliseconds.
In the meantime, code execution can not simply stop.
The page can't be allowed freeze while we wait for a response from the server.
Instead, I want to be able to pass a recipe or strategy for reacting to the receipt of a response.
This is the callback and it is a very common form of higher order function.
Instead of writing code which manages the tedium of checking potential responses and eventually queues up a behavior, the callback structure makes it possible to write a function which takes a response (as though I already have one).
When the response comes in, the higher order function will use the function passed to it, thus freeing me from thinking about/representing that particular concern.

### Mapping and Flatmapping

A recurring theme in FP is the use of mapping and flatmapping - extremely general, higher order functions to interact with values locked away inside of a special context.
One such context has already come up - the context of possibly existing in the future.
In javascript, this is referred to as a promise.
In Scala, you're more likely to see them referred to as `Future`s or `IO`s and they are type-parametric on their contents.

#### Map

Let's look at a few different ways of passing a function to `map`:
For this example, `List` is the context we'll use and it encodes the possibility of many (or 0) elements of type `X` given a `List[X]`.

```scala mdoc
val lst = List(1,2,3)
def times100(num: Int): Int = num * 100
lst.map(times100)
lst.map(_ * 100)
lst.map({ i: Int => i * 100})
```

For a given mappable/flatmappable context `C`, value `V1`, and function which takes a `V1` and returns a `V2`, using `map` ensures an output of `C[Int]`.
Here, `List[Int]` becomes `List[String]`:

```scala mdoc
lst.map(_.toString)
```

#### FlatMap

Flatmapping is similar, if a bit harder to get a handle on at first.
Whereas `map` transforms the underlying value but remains ignorant of context, `flatMap` takes an underlying value and can update both the underlying value and the context.

For this example, the `Option` structure will be used.
It is handy whenever a function may or may not have a result as is the case in standard division.
Dividing by 0 should be a `None` while dividing by another number should yield a `Some`.

```scala mdoc
def getPrice(id: String): Option[Double] =
  if (id == "000000-000000") None
  else Some(100)

Some("123abc-zxy321").flatMap(getPrice)
Some("000000-000000").flatMap(getPrice)
None.flatMap(getPrice)
```

### Filtering

Filtering is simpler than either of the above higher order functions.
To filter a `List[A]`, all that's required is a function `A => Boolean`.

```scala mdoc
val evens = 1 to 5 filter { num => num % 2 == 0 }
val odds = 1 to 5 filter { num => num % 2 == 1 }
```

### For Comprehension

When `map`, `flatMap`, and `filter` are defined for a structure (whether a `List`, or a `Future`, or an `Option`, or any other type) that structure can be used inside a [for comprehension](https://docs.scala-lang.org/tour/for-comprehensions.html).
Opinions among Scala developers bout the value of the `for comprehension` vary somewhat but they are generally seen as useful when you would otherwise chain together many `flatMap` calls manually

Constructing cartesian product of all even numbers from 1 and 5 and all odd numbers from 6 to 10:
```scala mdoc
for {
  even <- 1 to 5 if (even % 2 == 0)
  odd <- 6 to 10 if (odd % 2 == 1)
} yield (even, odd)
```

## Pay Attention to Implicits

The power of the compiler actually lets us do certain things which are inadvisable in other languages.
Implicit classes allow developers to define new methods for even private classes of another provenance.
Elsewhere this is called duck-typing and it is almost universally frowned upon.
The issue in a language like Python is that such method extensions will fail if and only if a dangerous code path is taken.
In Python, that bug might go undetected for months in production only to kill the entire long-running process.
Scala avoids this problem because the compiler is expressive enough to capture and anticipate many such bugs that would otherwise only be obvious after a crash - it is simply more difficult to write certain types of incorrect code in Scala.

This type-safe duck typing is exhibited in GeoTrellis' [GeoJSON serialization](vectors.md#features-and-json).
It is also apparent in some of the type signatures you'll find in the standard library.
We'll look to one of these as an example.
Here, a function for sorting anything that has a defined `Ordering` is defined:

```scala mdoc
def sortThings[Thing: Ordering](things: List[Thing]) = things.sorted
```

We can summon the implicit evidence for a given type.
```scala mdoc
implicitly[Ordering[Int]]
```

Defining new implicit instances isn't especially difficult.
Here, an `Ordering` is defined for fruits according to their taste:
```scala mdoc
import scala.math._
case class Fruit(name: String, tasteScore: Int)
implicit val tasteOrdering = new Ordering[Fruit] {
  def compare(a: Fruit, b: Fruit) = a.tasteScore compare b.tasteScore
}
```

Once we have the evidence that `Fruit` has an `Ordering`, we can order a `List[Fruit]`
```scala mdoc
val fruits = List(Fruit("apple", 7), Fruit("cavendish", 2), Fruit("mango", 9))
fruits.sorted
```
