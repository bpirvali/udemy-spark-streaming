package p2structuredstreaming

import org.apache.spark.sql.{DataFrame, Dataset, Encoders, SparkSession}
import org.apache.spark.sql.functions._
import common._

object StreamingDatasets {
  val spark = SparkSession.builder()
    .appName("Streaming Datasets")
    .master("local[2]")
    .getOrCreate()

  // include encoders for DF -> DS transformations
  import spark.implicits._
  def readCars(): Dataset[Car] = {
    // useful for DF -> DS transformations
    // implicit val carEncoder = Encoders.product[Car] <--- replaces spark.implicits._

    spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load() // DF with single string column "value"
      .select(from_json(col("value"), carsSchema).as("car")) // composite column (struct)
      .selectExpr("car.*") // DF with multiple columns
      .as[Car] // encoder can be passed implicitly with spark.implicits
  }

  def showCarNames() = {
    val carsDS: Dataset[Car] = readCars()

    // transformations here
    val carNamesDF: DataFrame = carsDS.select(col("Name")) // DF

    // collection transformations maintain type info
    val carNamesAlt: Dataset[String] = carsDS.map(_.Name)

    carNamesAlt.writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()
  }

  def ex1() = {
    val carsDS = readCars()
    carsDS.filter(_.Horsepower.getOrElse(0L) > 140)
      .writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()
  }

  def ex2() = {
    val carsDS = readCars()

    carsDS.select(avg(col("Horsepower")))
      .writeStream
      .format("console")
      .outputMode("complete")
      .start()
      .awaitTermination()
  }

  def ex3() = {
    val carsDS = readCars()

    val carCountByOrigin = carsDS.groupBy(col("Origin")).count() // option 1
    val carCountByOriginAlt = carsDS.groupByKey(car => car.Origin).count() // option 2 with the Dataset API

    carCountByOriginAlt
      .writeStream
      .format("console")
      .outputMode("complete")
      .start()
      .awaitTermination()
  }

  def main(args: Array[String]): Unit = {
//    showCarNames()
//    ex1()
//    ex2()
    ex3()
  }
}
