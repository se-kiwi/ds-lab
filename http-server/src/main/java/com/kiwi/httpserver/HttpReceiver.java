package com.kiwi.httpserver;

import com.kiwi.httpserver.config.Config;
import com.kiwi.httpserver.config.KafkaProperties;
import com.kiwi.httpserver.zookeeper.ZkDao;
import com.kiwi.httpserver.zookeeper.ZkDaoImpl;
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
    private ZkDao dao;

    private void initProducer() {
        this.topic = KafkaProperties.TOPIC;
        Properties properties = new Properties();
        properties.put("bootstrap.servers", KafkaProperties.BROKER_LIST);
        properties.put("acks", "all");
        properties.put("retries", 0);
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", StringSerializer.class.getName());
        producer = new KafkaProducer<String, String>(properties);
        dao  = new ZkDaoImpl();
    }

    public HttpReceiver() throws IOException {
        super(Config.SERVER_PORT);
        initProducer();
        System.out.println("Running!");
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);  // daemon true!
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
//        System.out.println("hit");
        if (method == Method.POST && uri.equals("/")) {
            try {
                byte[] buf;
                InputStream stream = session.getInputStream();
                buf = new byte[stream.available()];
//            assert stream.read(buf) == 0;
                if (stream.read(buf) != 0) {
                    throw new IOException();
                }
//            System.out.println(new String((buf)));
                producer.send(new ProducerRecord<String, String>(topic, "Message", new String(buf)));
            } catch (IOException e) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text", "wrong");
            }
            return newFixedLengthResponse("ok");
        }else if (method == Method.GET && uri.equals("/amount")){
            return newFixedLengthResponse(dao.getTotalTransactionAmount().toString());
//            return null;
        }else{
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text", "wrong");
        }
    }

    public static void main(String[] args) throws IOException {
        new HttpReceiver();
    }
}
