package com.kiwi.httpserver;

import com.google.gson.Gson;
import com.kiwi.httpserver.config.Conf;
import com.kiwi.httpserver.dto.OrderForm;
import com.kiwi.httpserver.mysql.MysqlDao;
import com.kiwi.httpserver.mysql.MysqlDaoImpl;
import com.kiwi.httpserver.zookeeper.ZkDao;
import com.kiwi.httpserver.zookeeper.ZkDaoImpl;
import fi.iki.elonen.NanoHTTPD;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static com.kiwi.httpserver.config.Conf.SERVER_PORT;

public class HttpReceiver extends NanoHTTPD {
    private String topic;
    private KafkaProducer<String, String> producer;
    private ZkDao zkDao = new ZkDaoImpl();
    private MysqlDao mysqlDao = new MysqlDaoImpl();
    private Gson gson = new Gson();

    private void initProducer() {
        this.topic = Conf.TOPIC;
        Properties properties = new Properties();
        properties.put("bootstrap.servers", Conf.BROKER_LIST);
        properties.put("acks", "all");
        properties.put("retries", 0);
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", StringSerializer.class.getName());
        producer = new KafkaProducer<String, String>(properties);
    }

    public HttpReceiver() throws IOException {
        super(SERVER_PORT);
        System.out.println("before init producer!");
        initProducer();
        System.out.println("after init producer!");
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);  // daemon true!
        System.out.println("Running!");
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        Map<String, String> parms = session.getParms();
//        System.out.println(uri);
//        System.out.println(method);
        if (method == Method.POST && uri.equals("/")) {
            String order_id = UUID.randomUUID().toString();
            try {
                char[] buf;
                InputStream stream = session.getInputStream();
                buf = new char[stream.available()];
                new BufferedReader(new InputStreamReader(stream))
                        .read(buf, 0, stream.available());
                String data = new String(buf);
                System.out.println(data);

                producer.send(new ProducerRecord<String, String>(topic, order_id, data));
                return newFixedLengthResponse(Response.Status.OK, "text", order_id);
            } catch (Exception e) {
                e.printStackTrace();
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text", "-1");
            }
        } else if (method == Method.GET) {
            switch (uri) {
                case "/amount":
                    return newFixedLengthResponse(zkDao.getTotalTransactionAmount().toString());
                case "/querybyid":
                    if (!parms.containsKey("id")) {
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text", "must have id");
                    }
                    return newFixedLengthResponse(gson.toJson(mysqlDao.getResultById(parms.get("id"))));
                case "/querybyuserid":
                    if (!parms.containsKey("userid")) {
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text", "must have user id");
                    }
                    return newFixedLengthResponse(gson.toJson(mysqlDao.getResultByUserId(parms.get("userid"))));
                default:
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text", "no such get uri");
            }
        } else {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text", "this method or uri not supported");
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java -jar <name.jar> MYSQL_PASSWORD");
            return;
        }
        Conf.MYSQL_PASSWD = args[0];
        new HttpReceiver();
    }
}
