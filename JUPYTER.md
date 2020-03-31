Using Scala in Jupyter
======================

Using the Almond Jupyter kernel, we can access a Spark-enabled Scala instance via Jupyter.  There are several modalities that we'd like to exploit.

## Installation

#### Local installation

I recommend a virtual environment setup for this option (the following uses virtualenv-wrapper for virtualenv management).

1. `mkvirtualenv scala-jupyter`
2. `pip install jupyter`
3. `export SCALA_VERSION=2.12.10`
4. `export ALMOND_VERSION=0.9.1`
5. In a temporary directory, execute the following:
```bash
curl -Lo coursier https://git.io/coursier-cli
chmod +x coursier
./coursier bootstrap \
    -r jitpack \
    -i user -I user:sh.almond:scala-kernel-api_$SCALA_VERSION:$ALMOND_VERSION \
    sh.almond:scala-kernel_$SCALA_VERSION:$ALMOND_VERSION \
    -o almond-2.12.10
```
6. `./almond-2.12.10 --install --force --id almond212 --display-name 'Scala (2.12.10)' --jupyter-path $VIRTUAL_ENV/share/jupyter/kernels`
7. Launch `jupyter notebook` (with any desired configs) and create a new notebook using the Scala (2.12.10) kernel

#### Docker

The best strategy here is to use one of the images supplied by the Almond project.

```bash
docker run -it --rm -p 8888:8888 almondsh/almond:0.9.1-scala-2.12.10
```

#### EMR

I've created the `feature/jupyter-scala` branch in the `https://github.com/geotrellis/geotrellis-deployments` repo which will set up an EMR cluster with `jupyterhub` installed on the master node.  **This branch depends on EMR 6.0.0-beta2.  There may be unanticipated problems because of this.** Check out that branch and follow these steps (all actions are taken in the `emr` subdirectory):

1. Copy all the `.mk.template` files to their corresponding `.mk` files.
2. Ensure that you have a current set of binary RPM dependencies copied to S3 and be sure that `S3_URI` and `RPMS_VERSION` in `config-run.mk` are appropriately set.  We are expecting the RPMs to be located in an S3 bucket of the form `${S3_URI}/rpms/${RPMS_VERSION}/`.
3. The `S3_NOTEBOOK_BUCKET` and `S3_NOTEBOOK_PREFIX` must be set for this to work, but this will allow persistence and/or sharing of notebooks.
4. Set all other parameters in the `.mk` files appropriately.
5. Issue `make create-cluster` and observe the progress in the AWS EMR console.
6. Grab the Master Public DNS field from the cluster console when the cluster is running and navigate to `<master public DNS>:8000`.  Log in using the highly secure credentials `user:password`.
7. Use the notebook as you please.
8. If you want to see the Spark console, you can issue `make proxy` and use FoxyProxy to tunnel into the cluster.
9. On completion, issue `make terminate-cluster`

## Usage

To get access to a Spark-enabled notebook, execute the following:

```scala
import $ivy.`org.apache.spark::spark-sql:2.4.5`
import $ivy.`org.locationtech.geotrellis::geotrellis-spark:3.2.0`

import org.apache.log4j.{Level, Logger}
Logger.getLogger("org").setLevel(Level.OFF)

import org.apache.spark.sql._

val spark = {
  NotebookSparkSession.builder()
    .master("local[*]")
    .getOrCreate()
}
```

Note the usage of `NotebookSparkSession` supercedes the typical `SparkSession`.  This also means that convenience functions that configure and build a SparkSession are off limits.

#### Usage on EMR

For EMR, the `SparkSession` should be constructed to point at a `yarn` master, but this may cause other issues.  I had to use the following command to build the Spark session object:

```scala
import org.apache.spark.sql._

val spark = {
  NotebookSparkSession.builder()
    .master("yarn")
    .config("spark.hadoop.yarn.timeline-service.enabled","false")
    .getOrCreate()
}
```

This will avoid a class not found exception related to Jersey.