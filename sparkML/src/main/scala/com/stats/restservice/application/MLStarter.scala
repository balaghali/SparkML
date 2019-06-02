package com.stats.restservice.application

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator

object MLStarter {

  def main(args: Array[String]): Unit = {
    val path = "SalesData.csv"
    val conf = new SparkConf().setMaster("local[2]").set("deploy-mode", "client").set("spark.driver.bindAddress", "127.0.0.1")
      .set("spark.broadcast.compress", "false")
      .setAppName("local-spark-kafka-consumer-client")
      .set("spark.sql.warehouse.dir", "/home/vagrant/Desktop/Workspaces/SparkMachineLearning/sparkML/src/main/resources/data")
    val sparkSession = SparkSession
      .builder()
      .config(conf)
      .getOrCreate()
    var data = sparkSession.read.format("csv").option("header", "true").option("inferSchema", "false").load(path)
    data.cache()
    import org.apache.spark.sql.DataFrameNaFunctions
    data.na.drop()
    data.show

    data.printSchema()

    import sparkSession.implicits._
    data = data.withColumn("SaleAmount", $"SaleAmount".cast(org.apache.spark.sql.types.DoubleType))
    data.show
    data.printSchema()

    //get monthly sales totals
    val summary = data.select("OrderMonthYear", "SaleAmount").groupBy("OrderMonthYear").sum().orderBy("OrderMonthYear").toDF("OrderMonthYear", "SaleAmount")
    summary.show

    // convert ordermonthyear to integer type
    //val results = summary.map(df => (df.getAs[String]("OrderMonthYear").replace("-", "") , df.getAs[String]("SaleAmount"))).toDF(["OrderMonthYear","SaleAmount"])
    import org.apache.spark.sql.functions._
    var test = summary.withColumn("OrderMonthYear", (regexp_replace(col("OrderMonthYear").cast("String"), "00:00:00", ""))).withColumn("OrderMonthYear", ((regexp_replace(col("OrderMonthYear").cast("String"), "-", "")))).toDF("OrderMonthYear", "SaleAmount")
    test.show
    import sparkSession.implicits._
    test = test.withColumn("OrderMonthYear", $"OrderMonthYear".cast(org.apache.spark.sql.types.IntegerType))
    test.show
    test.printSchema()

/*    val training = sparkSession.read.format("csv").option("header", "true").option("inferSchema", "false").load(path)
    training.cache()
    import org.apache.spark.sql.DataFrameNaFunctions
    training.na.drop()
    
    val lr = new LinearRegression()
  .setMaxIter(10)
  .setRegParam(0.3)
  .setElasticNetParam(0.8)

// Fit the model
val lrModel = lr.fit(training)

// Print the coefficients and intercept for linear regression
println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

// Summarize the model over the training set and print out some metrics
val trainingSummary = lrModel.summary
println(s"numIterations: ${trainingSummary.totalIterations}")
println(s"objectiveHistory: [${trainingSummary.objectiveHistory.mkString(",")}]")
trainingSummary.residuals.show()
println(s"RMSE: ${trainingSummary.rootMeanSquaredError}")
println(s"r2: ${trainingSummary.r2}")*/
    
    import org.apache.spark.ml.feature._
    val assembler = new VectorAssembler()
      .setInputCols(Array("OrderMonthYear", "SaleAmount"))
      .setOutputCol("features")
     
      /*import sparkSession.implicits._
    val output = assembler.transform(test)//.select($"SaleAmount".cast(DoubleType).as("label"), $"features".as("features"))
    output.show(false)
    output.printSchema()*/
    
    
val lr = new LinearRegression()
  .setMaxIter(100)
  .setRegParam(0.1)
  .setElasticNetParam(0.8)
  //.setFeaturesCol("features")   // setting features column
  .setLabelCol("SaleAmount")       // setting label column
  
  val lr1 = new LinearRegression()
  .setMaxIter(10)
  .setRegParam(100)
  .setElasticNetParam(0.8)
  //.setFeaturesCol("features")   // setting features column
  .setLabelCol("SaleAmount")       // setting label column
  .setPredictionCol("test")

//creating pipeline
val pipeline = new Pipeline().setStages(Array(assembler,lr))

//fitting the model
val lrModel = pipeline.fit(test)
      val predictionA = lrModel.transform(test).select($"SaleAmount".cast(DoubleType).as("label"), $"features".as("features") , $"prediction".as("prediction"))
      predictionA.show
    
val pipeline2 = new Pipeline().setStages(Array(assembler,lr1))

//fitting the model
val lrModel2 = pipeline2.fit(test)
      val predictionA1 = lrModel2.transform(test).select($"SaleAmount".cast(DoubleType).as("label"), $"features".as("features"), $"prediction".as("prediction"))
      predictionA1.show

      val evaluator = new RegressionEvaluator().setMetricName("rmse")
      var rmse = evaluator.evaluate(predictionA)
      println ("model 1 "+rmse)
      
      
      rmse = evaluator.evaluate(predictionA1)
      println ("model 2 "+rmse)
      
      
    

  }
}