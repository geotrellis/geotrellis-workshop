package workshop.util

import org.apache.spark._

object Spark {
  def context: SparkContext = {
    val conf = new SparkConf(loadDefaults =  true)
      .setMaster("local[*]")
      .setAppName("Local Console")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryo.registrator", "geotrellis.spark.store.kryo.KryoRegistrator")
    SparkContext.getOrCreate(conf)
  }
}