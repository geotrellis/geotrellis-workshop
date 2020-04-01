ThisBuild / scalaVersion := "2.12.10"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.azavea.geotrellis"
ThisBuild / organizationName := "GeoTrellis"
ThisBuild / useCoursier := false

lazy val root = (project in file("."))
  .settings(
    name := "geotrellis-workshop",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % Version.spark,
      "org.locationtech.geotrellis" %% "geotrellis-raster" % Version.geotrellis,
      "org.locationtech.geotrellis" %% "geotrellis-layer" % Version.geotrellis,
      "org.locationtech.geotrellis" %% "geotrellis-gdal" % Version.geotrellis,
      "org.locationtech.geotrellis" %% "geotrellis-spark" % Version.geotrellis,
      "org.locationtech.geotrellis" %% "geotrellis-s3" % Version.geotrellis,
      "org.scalatest" %% "scalatest" % "3.1.1" % Test
    ),
    resolvers ++= Seq(
      "eclipse-snapshots" at "https://repo.eclipse.org/content/groups/snapshots"),
    mdocVariables := Map(
      "VERSION" -> Version.geotrellis
    ),
    scalacOptions ++= List(
      "-deprecation",
      "-unchecked",
      "-feature",
      "-language:implicitConversions",
      "-language:reflectiveCalls",
      "-language:higherKinds",
      "-language:postfixOps",
      "-language:existentials",
      "-language:experimental.macros",
      "-Ypartial-unification", // Required by Cats
      "-Ydelambdafy:inline",
      "-target:jvm-1.8"
    ),
    console / initialCommands :=
      """
      import geotrellis.proj4._
      import geotrellis.vector._
      import geotrellis.raster._
      import geotrellis.layer._
      import geotrellis.spark._
      import org.apache.spark._
      """,
    initialize ~= { _ =>
      val ansi = System.getProperty("sbt.log.noformat", "false") != "true"
      if (ansi) System.setProperty("scala.color", "true")
    }
  )
  .enablePlugins(MdocPlugin, DocusaurusPlugin)
