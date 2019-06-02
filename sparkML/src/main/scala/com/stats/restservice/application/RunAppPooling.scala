package com.stats.restservice.application

import org.apache.spark.SparkConf
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.TimestampType
import org.apache.hadoop.mapred.InvalidInputException
import org.apache.spark.ml.regression.LinearRegression
import java.io.FileWriter
import org.apache.spark.mllib.util.MLUtils

object RunAppPooling {

  def main(args: Array[String]): Unit = { // start the spark session
    /*val spark  = SparkSession.builder()
    .appName("Spark XGBOOST Titanic Training")
    .master("local[*]")
    .config("spark.driver.bindAddress","127.0.0.1")
    .getOrCreate()*/

    val conf = new SparkConf().setMaster("local[2]").set("deploy-mode", "client").set("spark.driver.bindAddress", "127.0.0.1")
      .set("spark.broadcast.compress", "false")
      .setAppName("local-spark-kafka-consumer-client")
      .set("spark.sql.warehouse.dir", "/home/vagrant/Desktop/Workspaces/SparkMachineLearning/sparkML/src/main/resources/data")
    val spark = SparkSession
      .builder()
      .config(conf)
      .getOrCreate()

    // path where we have the training data
    val filePath = "src/main/resources/train_pooling.csv"
    val modelPath = "file:///home/vagrant/Downloads/medium-articles-master/titanic_spark/training_batch/src/main/resources/poolSessionModelRecent.model"

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

    while (true) {
      Thread.sleep(100)
      // read the raw data
      var df_raw = spark
        .read
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

      df_raw.show()
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

      // save the model
      cvModel.write.overwrite.save(modelPath)

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
        .csv("src/main/resources/test_pooling.csv")

      // fill all na values with 0
      val df1 = df_raw1.na.fill(0)
      df1.show
      /* extracted.printSchema
    extracted.show*/

      val evaluator = new RegressionEvaluator().setMetricName("rmse").setLabelCol("prediction")
      var rmse = evaluator.evaluate(cvModel.transform(df1))
      println("model 1 " + rmse)
      import org.apache.spark.sql.functions._
      import spark.implicits._
      val extracted = cvModel.transform(df1)
      extracted.show
      val prediction = extracted.select("prediction").map(r => r(0).asInstanceOf[Double]).collect()
      if (prediction != null && prediction.length > 0) {
        val avg = prediction.sum / prediction.length
        println(avg)
        //FIXME: publish to a topic
        val pw: FileWriter = new FileWriter("/home/vagrant/Desktop/Workspaces/SparkMachineLearning/sparkML/src/main/resources/result.csv");
        pw.append(avg.toString)
        pw.flush()
        pw.close()
        println("ss")
      } else {
        //publish no counter / reaminging same
      }

    }
  }

}
