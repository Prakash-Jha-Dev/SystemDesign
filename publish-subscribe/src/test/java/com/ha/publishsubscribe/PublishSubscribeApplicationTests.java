package com.ha.publishsubscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ha.publishsubscribe.domain.ConsumerGroup;
import com.ha.publishsubscribe.domain.Message;
import com.ha.publishsubscribe.domain.TopicDetail;
import com.ha.publishsubscribe.domain.Record;
import com.ha.publishsubscribe.interfaces.IConsumer;
import com.ha.publishsubscribe.interfaces.IConsumerGroup;
import com.ha.publishsubscribe.interfaces.IOffsetStore;
import com.ha.publishsubscribe.interfaces.IPartitioner;
import com.ha.publishsubscribe.interfaces.IProducer;
import com.ha.publishsubscribe.services.Consumer;
import com.ha.publishsubscribe.services.DataManager;
import com.ha.publishsubscribe.services.RecordProducer;
import com.ha.publishsubscribe.services.singleton.AllocationManager;
import com.ha.publishsubscribe.services.singleton.TopicManager;

@SpringBootTest
class PublishSubscribeApplicationTests {

	@Autowired
	private TopicManager topicManager;

	@Autowired
	private AllocationManager allocationManager;

	@Autowired
	private IProducer producer;

	@Autowired
	private IPartitioner partitioner;

	@Autowired
	private IOffsetStore offsetStore;

	class ProducerThread extends Thread {

		private IProducer producer;

		private List<Record> records;

		public <K,V>ProducerThread(IProducer producer, List<Record> records) {
			this.producer = producer;
			this.records = records;
		}

		public void run() {
			for (Record record: records) {
				producer.send(record);
			}
		}
	}

	class ConsumerThread extends Thread {

		private IConsumer consumer;

		public ConsumerThread(IConsumer consumer) {
			this.consumer = consumer;
		}

		public void run() {
			long startTime = System.nanoTime();
			int maxPollBeforeRequestingReallocation = 5;
			int currentRequestCount = 0;
			int secondMultiplier = 1000000000;
			double secondsToWait = 10;
			while (System.nanoTime() - startTime < 1L * secondsToWait * secondMultiplier) {
				consumer.consume(); 
				currentRequestCount++;
				if(currentRequestCount == maxPollBeforeRequestingReallocation) {
					Consumer tempConsumer = (Consumer)consumer;
					tempConsumer.requestReallocation();
				}
			}
		}
	}

	@Test
	public void setup() throws InterruptedException {
		List<DataManager> dataManagers = new ArrayList<>();
		for (int i=0; i<4; i++) {
			dataManagers.add(new DataManager(topicManager, offsetStore));
		}

		List<ProducerThread> producerThreads = new ArrayList<>();
		List<ConsumerThread> consumerThreads = new ArrayList<>();
		List<TopicDetail> topicDetails = new ArrayList<>();
		List<IConsumerGroup> consumerGroups = new ArrayList<>();

		for (int topic=0; topic<3; topic++) {
			String topicName = "topic_"+Integer.toString(topic);
			TopicDetail topicDetail = new TopicDetail(topicName, 6);
			topicDetails.add(topicDetail);
		}

		for (TopicDetail topicDetail: topicDetails) {
			topicManager.createTopic(topicDetail);

			RecordProducer recordProducer = new RecordProducer(topicDetail, partitioner);
			List<Record>records = new ArrayList<>();
			for (int topicMessageCount=0; topicMessageCount<20; topicMessageCount++) {
				Message<String,String> message = new Message<String,String>("MessageKey_"+UUID.randomUUID().toString(), "This message is record_"+Integer.toString(topicMessageCount)+" created during topic:"+topicDetail.getTopicName());
				records.add(recordProducer.createRecord(message));
			}

			producerThreads.add(new ProducerThread(producer, records));
			for (int consumerGroupForEachTopic=0 ; consumerGroupForEachTopic<2; consumerGroupForEachTopic++) {
				consumerGroups.add(new ConsumerGroup("consumerGroup_"+Integer.toString(consumerGroups.size()), topicDetail.getTopicName()));
				for (int consumerForEachConsumerGroup=0; consumerForEachConsumerGroup<4; consumerForEachConsumerGroup++) {
					consumerThreads.add(new ConsumerThread(new Consumer("consumer_"+Integer.toString(consumerThreads.size()), consumerGroups.get(consumerGroups.size()-1), topicManager, allocationManager)));
				}
			}
		}
		
		producerThreads.forEach(producer -> producer.start());
		consumerThreads.forEach(consumer -> consumer.start());

		producerThreads.forEach(producer -> {
			try {
				producer.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});

		consumerThreads.forEach(consumer -> {
			try {
				consumer.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		System.out.println("Done");
	}

}
