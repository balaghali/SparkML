# Predictive Analytics As a Service

This repository contains the code to setup Spark ML Stream processor in conjunction with built in java application. Please note that this is a POC , not a production ready code.

# Project Details 
- sparkML - Contains code for starting Spark Streaming ML processor written in SCALA.
- ApooledSession - Contains code to run a web application with rest end points written in JAVA.

# Build Instructions
- To build the Spark ML JAR, navigate to the SparkML folder and run **./gradlew clean build**.  The resulting JAR can be found at ../build/libs/***.jar.
- To build the java application JAR, navigate to the ApooledSession folder and run **./gradlew clean build**.  The resulting JAR can be found at ../build/libs/***.jar.

# Pre-requisites
- Java has to be installed on host machine.
- Enough runtime heap memory and ideal processors to run two jvm based applications
- Enough disk space to read and write data onto file system.


# RUN Instructions
- Navigate to the folder in which executable jar is present and run the following commands

#### Command ####

1) java -jar APooledSession.jar /home/vagrant/Downloads/DEMO/trainData.csv /home/vagrant/Downloads/DEMO/testData.csv /home/vagrant/Downloads/DEMO/result.csv
  
2) java -jar -Dsave.model.path="file:///home/vagrant/Downloads/DEMO/pooledModel" -DtrainingModel.path="file:///home/vagrant/Downloads/DEMO/trainData.csv" -DtestData.path="file:///home/vagrant/Downloads/DEMO/testData.csv" -Dresult.path="/home/vagrant/Downloads/DEMO/result.csv" sparkML-standalone.jar

# Arguments
#### Run time arguments of Java Application ####
- arg1 : fullyQualifiedPath of trainData file
- arg2 : fullyQualifiedPath of testData file
- arg3 : fullyQualifiedPath of result file

#### Run time system arguments of ML processor (-D args) ####
- save.model.path ~ fullyQualifiedPath with proto for saving the model
- trainingModel.path ~ fullyQualifiedPath with proto for trainingDataSet feeding
- testData.path ~ fullyQualifiedPath with proto for testDataSet feeding
- result.path ~ fullyQualifiedPath to write the result / prediction

**Note:** default values are hardcoded to be run on local machines which may not be working on each machine. Request to supply correct paths 

# Access Instructions:
- http://localhost:8009/ - SparkMLProcessor
- http://localhost:8080/ - JavaWebApplication

# Tech Stack
- Spark
- SparkMLLib
- Sockjs
- HTML
- WebSockets
- Vertx
- REST 
- Kafka

# Troubleshoot Instructions

- Look for any runtime exception in stdout logs that might halt the execution
- Loggers would be dumped for each interaction configured using sl4j , so keep an eye on loggers for more information.
- Spark history server / ui is accessible at 18080 port [history-server-link](http://ec2-54-71-162-138.us-west-2.compute.amazonaws.com:18080/history/)

# Tear down Instructions
- To stop the ml processor and java application . Please follow standard way of stopping jar applications.   

   **command : CTRL+C of running java processes , or force kill using kill command**

# Notifications
- Update about the status of different components can be seen on html forms. 
- Periodically with in few seconds new notifictions are pushed to browser , keep an eye on them.

# CI/CD 
#### TBD ####

# Miscallaneous
#### TBD ####

# Contact 
Please reach out to teja_ghali@yahoo.com or start a conversation here, for more information and additional assistance.

# Copyright
Copyright 2014-2019 @ Janasena org.
