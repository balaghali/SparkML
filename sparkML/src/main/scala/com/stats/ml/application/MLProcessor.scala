package com.stats.ml.application

import java.io.FileWriter

import org.apache.spark.SparkConf
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.TimestampType

import io.vertx.core.logging.LoggerFactory
import io.vertx.lang.scala.ScalaVerticle
import java.util.concurrent.atomic.AtomicBoolean
import io.vertx.scala.core.eventbus.Message
import org.apache.hadoop.conf.Configuration
import java.io.File
import java.io.PrintWriter

object MLProcessor extends Serializable {
  val modelPath = if (System.getProperty("save.model.path") != null) System.getProperty("save.model.path") else "file:///home/vagrant/Downloads/medium-articles-master/titanic_spark/training_batch/src/main/resources/poolSessionModelRecent.model"
  // path where we have the training data
  val filePath = if (System.getProperty("trainingModel.path") != null) System.getProperty("trainingModel.path") else "/home/vagrant/Desktop/Workspaces/SparkMachineLearning/sparkML/src/main/resources/train_pooling.csv" /*"src/main/resources/train_pooling.csv"*/
  val testDataPath = if (System.getProperty("testData.path") != null) System.getProperty("testData.path") else "/home/vagrant/Desktop/Workspaces/SparkMachineLearning/sparkML/src/main/resources/test_pooling.csv" /*"src/main/resources/test_pooling.csv"*/
  var resultPath = if (System.getProperty("result.path") != null) System.getProperty("result.path") else "/home/vagrant/Desktop/Workspaces/SparkMachineLearning/sparkML/src/main/resources/result.csv"
  // Issue with 2.12 , skipping for now
  val conf = new SparkConf().setMaster("local[2]").set("deploy-mode", "client").set("spark.driver.bindAddress", "127.0.0.1")
    .set("spark.broadcast.compress", "false")
    .setAppName("local-spark-kafka-consumer-client")
    .set("spark.sql.warehouse.dir", "file:/tmp/spark-warehouse")

  val spark = SparkSession
    .builder()
    .config(conf)
    .getOrCreate()

  val hadoopConfig = spark.sparkContext.hadoopConfiguration
  /*hadoopConfig.set(
      "fs.hdfs.impl",
      classOf[org.apache.hadoop.hdfs.DistributedFileSystem].getName)*/
  hadoopConfig.set(
    "fs.file.impl",
    classOf[org.apache.hadoop.fs.LocalFileSystem].getName)

  val inProcess = new AtomicBoolean(false)

  println("modelPath --" + modelPath)
  println("filePath --" + filePath)
  println("testDataPath --" + testDataPath)
  println("testDataPath --" + testDataPath)

  def main(args: Array[String]): Unit = {
//    import java.nio.file.{ Paths, Files }
//        import java.nio.charset.StandardCharsets
//        Files.write(Paths.get("/home/vagrant/Downloads/DEMO/result.txt"), "1".getBytes(StandardCharsets.UTF_8))
    /*if (!inProcess.get) {
      process()
      inProcess.set(true)
    }*/
  }

  def processWrapper(): Double = {
    if (!inProcess.get) {
      println("processing")
      val result = process()
      inProcess.set(true)
      result
    } else {
      println("skipping processing")
      0.001
    }
  }

  def process(): Double = {

    // start the spark session
    /*val spark  = SparkSession.builder()
    .appName("Spark XGBOOST Titanic Training")
    .master("local[*]")
    .config("spark.driver.bindAddress","127.0.0.1")
    .getOrCreate()*/

    val schema = StructType(
      Array(
        StructField("PACKAGE_KEY", StringType),
        StructField("MOST_IDLE", IntegerType),
        StructField("MAX_WAIT", IntegerType),
        StructField("IDLE_COUNT", IntegerType),
        StructField("APPLICATION", StringType),
        StructField("LONGEST_WAIT", IntegerType),
        StructField("TIMEOUTS", IntegerType),
        StructField("LAST_ACCESS", TimestampType),
        StructField("MOST_ACTIVE", IntegerType),
        StructField("MAX_ACTIVE", IntegerType),
        StructField("MAX_IDLE", IntegerType),
        StructField("ACTIVE_COUNT", IntegerType),
        StructField("FACTOR_LOAD", DoubleType)))

    // read the raw data
    var df_raw = spark
      .read
      .format("org.apache.spark.sql.execution.datasources.csv.CSVFileFormat")
      .option("header", "true")
      //      .option("inferSchema","true")
      .schema(schema)
      .csv(filePath)

    df_raw = df_raw.drop(df_raw.col("PACKAGE_KEY"))
    df_raw = df_raw.drop(df_raw.col("MOST_IDLE"))
    df_raw = df_raw.drop(df_raw.col("MAX_IDLE"))
    df_raw = df_raw.drop(df_raw.col("MOST_ACTIVE"))
    df_raw = df_raw.drop(df_raw.col("LAST_ACCESS"))
    df_raw = df_raw.drop(df_raw.col("APPLICATION"))
    df_raw = df_raw.drop(df_raw.col("MAX_WAIT"))

    //      df_raw.show()
    //    println(df_raw.count())
    // fill all na values with 0
    val df = df_raw.na.fill(0)
    df.printSchema()
    val packageKeyIndexer = new StringIndexer()
      .setInputCol("PACKAGE_KEY")
      .setOutputCol("PackageIndex")
      .setHandleInvalid("keep")

    // create the feature vector
    val vectorAssembler = new VectorAssembler()
      .setInputCols(Array("IDLE_COUNT", "TIMEOUTS", "ACTIVE_COUNT" /*, "TOTAL_REQUEST_COUNT"*/ ))
      .setOutputCol("features_intermediate")

    /* var lr1: PipelineModel = null
    try {
      lr1 = PipelineModel.read.load(modelPath)
//      lr1.
    } catch {
      case ie: InvalidInputException => println(ie.getMessage)
    }
*/
    import org.apache.spark.ml.feature.StandardScaler
    val scaler = new StandardScaler().setWithMean(true).setWithStd(true).setInputCol("features_intermediate").setOutputCol("features")

    var pipeline: Pipeline = null
    //    if (lr1 == null) {
    val lr =
      new LinearRegression()
        .setMaxIter(100)
        .setRegParam(0.1)
        .setElasticNetParam(0.8)
        //.setFeaturesCol("features")   // setting features column
        .setLabelCol("FACTOR_LOAD") // setting label column
    // create the pipeline with the steps
    pipeline = new Pipeline().setStages(Array( /*genderIndexer, cabinIndexer, embarkedIndexer,*/ vectorAssembler, scaler, lr))
    /*   } else {
      pipeline = new Pipeline().setStages(Array( lr1))
    }*/

    // create the model following the pipeline steps
    val cvModel = pipeline.fit(df)

    // save the model - to avoid permisssion issues
    //cvModel.write.overwrite.save(modelPath)

    var testschema = StructType(
      Array(
        //        StructField("PACKAGE_KEY", StringType),
        StructField("IDLE_COUNT", IntegerType),
        StructField("TIMEOUTS", IntegerType),
        StructField("ACTIVE_COUNT", IntegerType)))

    val df_raw1 = spark
      .read
      //      .option("header", "true")
      .schema(testschema)
      .csv(testDataPath)

    // fill all na values with 0
    val df1 = df_raw1.na.fill(0)
    //      df1.show
    /* extracted.printSchema
    extracted.show*/

    val evaluator = new RegressionEvaluator().setMetricName("rmse").setLabelCol("prediction")
    var rmse = evaluator.evaluate(cvModel.transform(df1))
    //      vertx.eventBus().publish("events-feed","RMSE of model " + rmse)
    import org.apache.spark.sql.functions._
    import spark.implicits._
    val extracted = cvModel.transform(df1)
    //      extracted.show
    val prediction = extracted.select("prediction").map(r => r(0).asInstanceOf[Double]).collect()
    if (prediction != null && prediction.length > 0) {
      val avg = prediction.sum / prediction.length
      println(avg)
      //FIXME: publish to a topic
      val file = new File(resultPath)
      if (file.exists()) {
        println(file.getAbsolutePath)
                println(file.getCanonicalPath)
        import java.nio.file.{ Paths, Files }
        import java.nio.charset.StandardCharsets
        Files.write(Paths.get(resultPath), avg.toString.getBytes(StandardCharsets.UTF_8))
      }
      println("completed modelling process")
    } else {
      //publish no counter / reaminging same
    }

    rmse
  }

}