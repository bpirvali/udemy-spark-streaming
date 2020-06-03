package p1recap

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, _}



object SparkRecap {
  val spark = SparkSession.builder()
    .appName("Spark Recap")
    .master("local[2]")
    .getOrCreate()

  // read a DF
  val cars = spark.read
    .format("json")
    .option("inferSchema", "true")
    .load("src/main/resources/data/cars")

  import spark.implicits._
  val usefulCarsData = cars.select(
    col("Name"),
    $"Year", // <-- needs implicits,
    (col("Weight_in_lbs") / 2.2 ).as("Weight_in_kg"),
    expr("Weight_in_lbs / 2.2").as("Weight_in_kg_too")
  )

  // expression
  val carsWeights_kg = cars.selectExpr("Weight_in_lbs / 2.2")

  // filter
  val europeanCars = cars.where(col("Origin") =!= "USA")

  // aggregation
  val averageHP = cars.select(avg(col("Horsepower")).as("average_hp"),
    min(col("Horsepower")).as("min_hp")) // sum, meam, stddev, min, max

  // grouping
  val countByOrigin = cars
    .groupBy(col("Origin")) // a RelationalGroupedDataset
    .count()

  // joining
  /*
    join types
    - inner: only the matching rows are kept
    - left/right/full outer join
    - semi/anti
   */
  val guitarPlayers = spark.read
    .option("inferSchema", "true")
    .json("src/main/resources/data/guitarPlayers")

  val bands = spark.read
    .option("inferSchema", "true")
    .json("src/main/resources/data/bands")

  val guitaristsBands = guitarPlayers.join(bands, guitarPlayers.col("band") === bands.col("id"))

  // ------------------------------------------------------
  // datasets = typed distributed collection of objects
  // ------------------------------------------------------
  case class GuitarPlayer(id: Long, name: String, guitars: Seq[Long], band: Long)

  // converts a DF to a DataSet
  val guitarPlayersDS = guitarPlayers.as[GuitarPlayer] // needs spark.implicits
  guitarPlayersDS.map(_.name) //

  // Spark Structured API = Spark SQL + DF API
  cars.createOrReplaceTempView("cars")
  val americanCars = spark.sql(
    """
      |select Name from cars where Origin = 'USA'
    """.stripMargin
  )

  // low-level API: RDDs
  val sc = spark.sparkContext
  val numbersRDD: RDD[Int] = sc.parallelize(1 to 1000000)

  // functional operators
  val doubles = numbersRDD.map(_ * 2)

  // RDD -> DF
  val numbersDF = numbersRDD.toDF("number") // you lose type info, you get SQL capability

  // RDD -> DS ... keeps the type info as well as you have SQL capabilities
  val numbersDS = spark.createDataset(numbersRDD)

  // DS -> RDD
  val guitarPlayersRDD = guitarPlayersDS.rdd

  // DF -> RDD
  val carsRDD = cars.rdd // RDD[Row] ...Row untyped collection of arbitrary information

  def main(args: Array[String]): Unit = {
    cars.show()
    cars.printSchema()
  }
}
