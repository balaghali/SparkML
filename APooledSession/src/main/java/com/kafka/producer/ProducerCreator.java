package com.kafka.producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.kafka.constants.IKafkaConstants;

public class ProducerCreator {

	public static Producer<Long, String> createProducer(String aKafkaBrokerList) {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, setKafkaBrokerList(aKafkaBrokerList));
		props.put(ProducerConfig.CLIENT_ID_CONFIG, IKafkaConstants.CLIENT_ID);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		return new KafkaProducer<>(props);
	}

	private static String setKafkaBrokerList(String aKafkaList) {
		if(aKafkaList != null && aKafkaList.length() > 0) {
			return aKafkaList;
		}
		return IKafkaConstants.KAFKA_BROKERS;
	}
}