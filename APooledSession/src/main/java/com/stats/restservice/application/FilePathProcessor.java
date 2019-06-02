package com.stats.restservice.application;

import java.io.File;
import java.io.FileWriter;
import java.util.stream.Stream;

import com.opencsv.CSVWriter;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class FilePathProcessor {

	public String mTrainingDataFilePath = "/home/vagrant/Desktop/Workspaces/SparkMachineLearning/sparkML/src/main/resources/train_pooling.csv";
	public String mTestDataFilePath = "/home/vagrant/Desktop/Workspaces/SparkMachineLearning/sparkML/src/main/resources/test_pooling.csv";
	public String mResultFilePath = "/home/vagrant/Desktop/Workspaces/SparkMachineLearning/sparkML/src/main/resources/result.csv";

	static Logger logger = LoggerFactory.getLogger(FilePathProcessor.class);

	public FilePathProcessor(String mTrainingDataFilePath, String mTestDataFilePath, String mResultFilePath) {
		super();
		this.mTrainingDataFilePath = mTrainingDataFilePath;
		this.mTestDataFilePath = mTestDataFilePath;
		this.mResultFilePath = mResultFilePath;
	}

	public FilePathProcessor() {
	}

	public FilePathProcessor(String[] args) {
		if (args == null || args.length != 3) {
			System.out.println("Incorrect number of arguments received, default to local values");
			return;
		}
		this.mTrainingDataFilePath = args[0];
		this.mTestDataFilePath = args[1];
		this.mResultFilePath = args[2];
		logger.info(this);
		createFiles();
	}
	
	public static void main(String[] args) throws InterruptedException {
		try {
			File file = new File("/home/vagrant/Downloads/DEMO/result.csv");

			CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
			String[] entries = { "1"};
			csvWrite.writeNext(entries);
			csvWrite.close();
		} catch (Exception e) {
			logger.error("Error while creating result file "  + e) ;
			System.exit(1);
		} finally {
			
		}

	}

	private void createFiles() {
		
		
		/*Stream.of(mTestDataFilePath, mResultFilePath).forEach(path -> {
			try {
				File file = new File(path);

				// Create the file
				// FIXME: have to create header
				if (file.createNewFile()) {
					logger.info("File is created!");
				} else {
					logger.info("File already exists.");
				}

			} catch (Exception e) {
				logger.error("Error while creating file " + path);
				System.exit(1);
			}
		});*/
		try {
			File file = new File(mTrainingDataFilePath);
			CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
			String[] entries = { "PACKAGE_KEY", "MOST_IDLE", "MAX_WAIT", "IDLE_COUNT", "APPLICATION", "LONGEST_WAIT",
					"TIMEOUTS", "LAST_ACCESS", "MOST_ACTIVE", "MAX_ACTIVE", "MAX_IDLE", "ACTIVE_COUNT", "FACTOR_LOAD" };
			csvWrite.writeNext(entries);
			csvWrite.close();
		} catch (Exception e) {
			logger.error("Error while creating file " + mTrainingDataFilePath + e);
			System.exit(1);
		} finally {
			
		}
		
		try {
			File file = new File(mTestDataFilePath);
			CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
			String[] entries = { "0","0","0" };
			csvWrite.writeNext(entries);
			csvWrite.close();
		} catch (Exception e) {
			logger.error("Error while creating test file " + mTestDataFilePath);
			System.exit(1);
		} finally {
			
		}
		
		try {
			File file = new File(mResultFilePath);

			if (file.createNewFile()) {
				logger.info("File result is created!");
			} else {
				logger.info("File already exists.");
			}

			/*CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
			String[] entries = { "1"};
			csvWrite.writeNext(entries);
			csvWrite.close();*/
		} catch (Exception e) {
			logger.error("Error while creating result file " + mResultFilePath + e) ;
			System.exit(1);
		} finally {
			
		}

	}

	@Override
	public String toString() {
		return "FilePathDTO [mTrainingDataFilePath=" + mTrainingDataFilePath + ", mTestDataFilePath="
				+ mTestDataFilePath + ", mResultFilePath=" + mResultFilePath + "]";
	}
}
