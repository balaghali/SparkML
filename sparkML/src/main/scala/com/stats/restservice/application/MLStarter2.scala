package com.stats.restservice.application

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.SparkConf
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.evaluation.RegressionEvaluator

object MLStarter2 {

  def main(args: Array[String]): Unit = {// start the spark session
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
    val filePath = "src/main/resources/train.csv"
    val modelPath = "file:///home/vagrant/Downloads/medium-articles-master/titanic_spark/training_batch/src/main/resources/trainedModel.model"

    val schema = StructType(
      Array(StructField("PassengerId", DoubleType),
        StructField("Survival", DoubleType),
        StructField("Pclass", DoubleType),
        StructField("Name", StringType),
        StructField("Sex", StringType),
        StructField("Age", DoubleType),
        StructField("SibSp", DoubleType),
        StructField("Parch", DoubleType),
        StructField("Ticket", StringType),
        StructField("Fare", DoubleType),
        StructField("Cabin", StringType),
        StructField("Embarked", StringType)
      ))

    // read the raw data
    val df_raw = spark
      .read
      .option("header", "true")
      .schema(schema)
      .csv(filePath)

    // fill all na values with 0
    val df = df_raw.na.fill(0)

    // convert nominal types to numeric for the columns, sex, cabin and embarked
    val sexIndexer = new StringIndexer()
      .setInputCol("Sex")
      .setOutputCol("SexIndex")
      .setHandleInvalid("keep")

    val cabinIndexer = new StringIndexer()
      .setInputCol("Cabin")
      .setOutputCol("CabinIndex")
      .setHandleInvalid("keep")

    val embarkedIndexer = new StringIndexer()
      .setInputCol("Embarked")
      .setOutputCol("EmbarkedIndex")
      .setHandleInvalid("keep")

    // create the feature vector
    val vectorAssembler = new VectorAssembler()
      .setInputCols(Array("Pclass", "SexIndex", "Age", "SibSp", "Parch", "Fare", "CabinIndex", "EmbarkedIndex"))
      .setOutputCol("features")
      
    val lr1 = new LinearRegression()
  .setMaxIter(100)
  .setRegParam(0.0)
  .setElasticNetParam(0.8)
  //.setFeaturesCol("features")   // setting features column
  .setLabelCol("Survival")    // setting label column

      
    // create the pipeline with the steps
    val pipeline = new Pipeline().setStages(Array(sexIndexer, cabinIndexer, embarkedIndexer, vectorAssembler, lr1))

    // create the model following the pipeline steps
    val cvModel = pipeline.fit(df)

    // save the model
    cvModel.write.overwrite.save(modelPath)

    
    var testschema = StructType(
      Array(StructField("PassengerId", DoubleType),
        StructField("Pclass", DoubleType),
        StructField("Name", StringType),
        StructField("Sex", StringType),
        StructField("Age", DoubleType),
        StructField("SibSp", DoubleType),
        StructField("Parch", DoubleType),
        StructField("Ticket", StringType),
        StructField("Fare", DoubleType),
        StructField("Cabin", StringType),
        StructField("Embarked", StringType)
      ))
    
     val df_raw1 = spark
      .read
      .option("header", "true")
      .schema(testschema)
      .csv("src/main/resources/test.csv")

    // fill all na values with 0
    val df1 = df_raw1.na.fill(0)
    
    val evaluator = new RegressionEvaluator().setMetricName("rmse").setLabelCol("prediction")
    val extracted = cvModel.transform(df1)
    extracted.show()
    var rmse = evaluator.evaluate(extracted)//.select( $"Survival".as("label")))
    println ("model 1 "+rmse)
  }

}
