package com.kiwi.httpserver;

import fi.iki.elonen.NanoHTTPD;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HttpReceiver extends NanoHTTPD {
    private String topic;
    private KafkaProducer<String, String> producer;

    private void initProducer() {
        this.topic = KafkaProperties.TOPIC;
        Properties properties = new Properties();
        properties.put("bootstrap.servers", KafkaProperties.BROKER_LIST);
        properties.put("acks", "all");
        properties.put("retries", 0);
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", StringSerializer.class.getName());
        producer = new KafkaProducer<String, String>(properties);
    }

    public HttpReceiver() throws IOException {
        super(Config.PORT);
        initProducer();
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);  // daemon true!
        System.out.println("Running!");
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            byte[] buf;
            InputStream stream = session.getInputStream();
            buf = new byte[stream.available()];
//            assert stream.read(buf) == 0;
            if (stream.read(buf) != 0){
                throw new IOException();
            }
//            System.out.println(new String((buf)));
            producer.send(new ProducerRecord<String, String>(topic, "Message", new String(buf)));
        } catch (IOException e) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text", "wrong");
        }
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text", "wrong");
    }

    public static void main(String[] args) throws IOException {
        new HttpReceiver();
    }
}
