package com.stats.restverticle;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.adolf.pool.manager.RequestorPoolDetailsDTO;
import com.adolf.pool.manager.RequestorPoolManager;
import com.kafka.constants.IKafkaConstants;
import com.kafka.producer.ProducerCreator;
import com.stats.restservice.application.FilePathProcessor;
import com.stats.restservice.external.services.IStatisticsService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Verticle that periodically removes the transactions that are older than 60
 * seconds
 */
public class PeriodicCleanerVerticle extends AbstractVerticle {

	private static String RESULT_FILE_PATH = "";
	private IStatisticsService mStatService;
	private static final Logger logger = LoggerFactory.getLogger(PeriodicCleanerVerticle.class);
	private long mLastTimeoutCount = 0;
	private AtomicLong mKafaKey = new AtomicLong(0);
	private Producer<Long, String> mProducer = null;

	/**
	 * Currently being used for junit purpose, depending on requirements we can
	 * consider custom delay at later point of time
	 */
	private int mDelay = 3000;
	private long mID;
	private boolean shouldRead = false;
	private AtomicBoolean isEnabled = new AtomicBoolean(false);
	private String mTrainDataFilePath  = "";
	private String mTestDataFilePath = "";

	public PeriodicCleanerVerticle(IStatisticsService statisticsService,FilePathProcessor dto) {
		this(statisticsService, -1);
		RESULT_FILE_PATH = dto.mResultFilePath;
		mTrainDataFilePath = dto.mTrainingDataFilePath;
		mTestDataFilePath = dto.mTestDataFilePath;
	}

	/**
	 * To be used in conjunction with junit tests
	 * 
	 * @param statisticsService
	 * @param delay
	 */
	PeriodicCleanerVerticle(IStatisticsService statisticsService, int delay) {
		mStatService = statisticsService;
		mDelay = delay > 0 ? delay : mDelay;
	}

	/**
	 * Invoked when this verticle is deployed. (Life cycle method to start the
	 * verticle)
	 */
	@Override
	public void start() throws Exception {
		// This timer will be caller every 12 seconds - log error and ignore any
		// exceptions so that event loop will be intact
		mID = vertx.setPeriodic(mDelay, id -> {
			try {
				logger.info("About to write data to kafka / csv");
				writeDataToCSV();
				// mStatService.removeStaleTransactions();
			} catch (Exception e) {
				logger.error("Encountered exception while writting transaction data" + e);
			}
		});

		vertx.eventBus().consumer("pool-config", message -> {
			String msgBody = (String) message.body(); // Caution - does this cause any issues ??

			if (msgBody.contains("StartPublish")) {
				logger.info("Begin: Collecting data for machine learning");
				isEnabled.set(true);
			}
			if (msgBody.contains("StopPublish")) {
				logger.info("Stopeed collecting data for machine learning");
				isEnabled.set(false);
			}
		});
	}

	private void writeDataToCSV() {
		if (!isEnabled.get()) {
			String message = "Not collecting data - periodicCleanreVerticle";
			logger.info(message);
			vertx.eventBus().publish("events-feed", "Pool configuration is not defined..");
			vertx.eventBus().publish("pool-feed", "Click StartPublish to write data to kafka queue..");
			try {
				// TODO : are we clearing out every time ??
				FileChannel.open(Paths.get(RESULT_FILE_PATH), StandardOpenOption.WRITE).truncate(0).close();
			} catch (IOException e) {
				logger.warn("Clearing result csv failed");
			}
			return;
		}
		List<RequestorPoolDetailsDTO> requestorPoolDetails = RequestorPoolManager.getInstance()
				.getRequestorPoolDetails();
		RequestorPoolDetailsDTO dto1 = requestorPoolDetails.get(requestorPoolDetails.size() - 1);
		vertx.eventBus().publish("events-feed", "Collecting data to be sent over kafka for data processing..");
		try {
			FileWriter pw = new FileWriter(mTrainDataFilePath  ,true);
			requestorPoolDetails.forEach(dto -> {
				try {
					logger.info("Started appending data to csv");
					pw.append("\n");
					// PACKAGE_KEY MOST_IDLE MAX_WAIT IDLE_COUNT APPLICATION LONGEST_WAIT TIMEOUTS
					// LAST_ACCESS MOST_ACTIVE MAX_ACTIVE MAX_IDLE ACTIVE_COUNT
					double loadFactor = ((dto1.getActiveCount() + Math.abs(dto1.getTimeoutCount() - mLastTimeoutCount))
							/ dto1.getMaxActiveCount());
					pw.append(dto.getServicePackageName());
					pw.append(",");
					pw.append(String.valueOf(dto.getMostIdleCount()));
					pw.append(",");
					pw.append(String.valueOf(dto.getMaxWaitTime()));
					pw.append(",");
					pw.append(String.valueOf(dto.getIdleCount()));
					pw.append(",");
					pw.append(dto.getApplicationInfo());
					pw.append(",");
					pw.append(String.valueOf(dto.getLongestWaitTime()));
					pw.append(",");
					pw.append(String.valueOf(Math.abs(dto.getTimeoutCount() - mLastTimeoutCount)));
					pw.append(",");
					pw.append(dto.getLastAccessTime());
					pw.append(",");
					pw.append(String.valueOf(dto.getMostActiveCount()));
					pw.append(",");
					pw.append(String.valueOf(dto.getMaxActiveCount()));
					pw.append(",");
					pw.append(String.valueOf(dto.getMaxIdleCount()));
					pw.append(",");
					pw.append(String.valueOf(dto.getActiveCount()));
					pw.append(",");
					pw.append(String.valueOf(loadFactor));
					try {
						final ProducerRecord<Long, String> record = new ProducerRecord<>(IKafkaConstants.TOPIC_NAME,
								dto.toString());
						if (mProducer == null)
							mProducer = ProducerCreator.createProducer(IKafkaConstants.KAFKA_BROKERS);
						mProducer.send(record, (metadata, exception) -> {
							if (metadata != null) {
								/*
								 * System.out.printf("sent record(key=%s value=%s) " +
								 * "meta(partition=%d, offset=%d) \n", record.key(), record.value(),
								 * metadata.partition(), metadata.offset());
								 */
							} else {
								logger.error(exception.getMessage());
							}
						});
					} catch (Exception e) { // Lets not worry for now...
						logger.error("Error in sending record");
						logger.error(e);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			pw.flush();
			pw.close();
			FileWriter pw1 = new FileWriter(mTestDataFilePath ,false);
			pw1.append(String.valueOf(dto1.getIdleCount()));
			pw1.append(",");
			pw1.append(String.valueOf(Math.abs(dto1.getTimeoutCount() - mLastTimeoutCount)));
			pw1.append(",");
			pw1.append(String.valueOf(dto1.getActiveCount()));
			pw1.flush();
			pw1.close();
			logger.info("Completed writing data to train and test csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//CODE is not formalized , only for poc purpose
		try {
			String text = new String(Files.readAllBytes(Paths.get(RESULT_FILE_PATH)), StandardCharsets.UTF_8);
			text = (text == null || text.length() == 0) ? "0.0" : text;
			double loadFactor = ((dto1.getActiveCount() + Math.abs(dto1.getTimeoutCount() - mLastTimeoutCount))
					/ dto1.getMaxActiveCount());
			String message = "predicted load Factor :" + text + ", actualLoadFactor : " + loadFactor;
			logger.info(message);
			vertx.eventBus().publish("pool-feed", message);
			vertx.eventBus().publish("pool-feed", "performance Index : "+ dto1.getActiveCount()/dto1.getTimeoutCount() );
			String poolJson = "Pool Configuration is succinct...";
			double predictedLoad = Double.parseDouble(text);
			if(predictedLoad < loadFactor) {
				poolJson = "Current Pool Configuration is underperforming \n with current transaction receival rate.. ";
			}
			vertx.eventBus().publish("pool-json", poolJson);
//			FileWriter pw1 = new FileWriter(
//					"/home/vagrant/Desktop/Workspaces/SparkMachineLearning/sparkML/src/main/resources/load.csv", true);
//			pw1.append(" \n predicted load Factor :" + text + ", actualLoadFactor" + loadFactor);
			mLastTimeoutCount = dto1.getTimeoutCount();
//			pw1.flush();
//			pw1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Hack to delay the reading of predicted output by ML processor
		if (shouldRead) {
			shouldRead = false;
		} else {
			shouldRead = true;
		}
	}

	/**
	 * To be used only for junit to cancel the periodic timers
	 * 
	 * @return
	 */
	long getPeriodicVerticleId() {
		return mID;
	}
}
