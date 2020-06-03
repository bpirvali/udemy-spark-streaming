package p2structuredstreaming

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import common._
import org.apache.spark.sql.streaming.Trigger
//import org.apache.spark.unsafe.types.UTF8String
import p1recap.SparkRecap.spark
import org.apache.spark.sql.types._
import scala.concurrent.duration._

object StreamingDataFrames {
  val spark = SparkSession.builder()
    .appName("Our first streams")
    .master("local[2]")
    .getOrCreate()

  def readFromSocket() = {
    // reading a DF
    val lines: DataFrame = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load()

    // transformation
    //val shortLines: DataFrame = lines.filter(length(col("value")) <= 5)

    // tell between a static vs a streaming DF
    //println(shortLines.isStreaming)

    // consuming a DF
    val query = lines.writeStream
      .format("console")
      .outputMode("append")
      .start()

    // wait for the stream to finish
    query.awaitTermination()
  }

  def readFromFiles() = {
    val stocksDF: DataFrame = spark.readStream
      .format("csv")
      .option("header", "false")
      .option("dateFormat", "MMM d yyyy")
      .schema(stocksSchema)
      .load("src/main/resources/data/stocks")

    stocksDF.writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()
  }

  def demoTriggers() = {
    val lines: DataFrame = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load()

    import spark.implicits._
    //val lines2 = lines.select(col("value").cast("integer").as("number"))

    // write the lines DF at a certain trigger
    lines.writeStream
      .format("console")
      .outputMode("append")
      .trigger(
        //Trigger.ProcessingTime(2.seconds) // every 2 seconds run the query
        // Trigger.Once() // single batch, then terminate
        Trigger.Continuous(5.seconds) // experimental, every 2 seconds create a batch with whatever you have
      )
      .start()
      .awaitTermination()
  }
  def main(args: Array[String]): Unit = {
    //readFromSocket()
    //readFromFiles()
    demoTriggers()
  }
}
