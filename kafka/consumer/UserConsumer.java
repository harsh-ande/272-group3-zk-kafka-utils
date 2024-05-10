package edu.sjsu.cmpe272.simpleblog.server.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;

@Service
public class UserConsumer {

    @KafkaListener(id = "${consumer.listener.id}", topicPartitions = {
            @TopicPartition(topic = "${user.topic.name}", partitions = "#{partitionCalculator.calculatePartitions()}")},
            groupId = "${consumer.group.id}")
    public void consumeFromDynamicPartitions(ConsumerRecord<String, String> record) {
        System.out.println("Received message: " + record.value() + " from partition: " + record.partition());
        // Process the consumed message here
    }
}

