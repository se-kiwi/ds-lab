package com.kiwi.dslab;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class MyKafkaProducer extends Thread {
    private String topic;
    private KafkaProducer<String, String> producer;

    public MyKafkaProducer(String topic) {
        this.topic = topic;
        Properties properties = new Properties();
        properties.put("bootstrap.servers", KafkaProperties.BROKER_LIST);
        properties.put("acks", "all");
        properties.put("retries", 0);
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", StringSerializer.class.getName());
        producer = new KafkaProducer<String, String>(properties);
    }

    @Override
    public void run() {
        try {
            int messageNo = 1;
            while (true) {
                String messageStr = "Hello, this is No. " + messageNo + " data.";
                producer.send(new ProducerRecord<String, String>(topic, "Message", messageStr));
                if (messageNo % 100 == 0) {
                    System.out.println("Sent: " + messageStr);
                }
                if (messageNo % 1000 == 0) {
                    System.out.println("Sent " + messageStr + " msgs");
                    break;
                }
                messageNo++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }

    public static void main(String[] args) {
        new MyKafkaProducer(KafkaProperties.TOPIC).start();
    }
}
