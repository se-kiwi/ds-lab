package com.kiwi.dslab;

import com.google.gson.Gson;
import com.kiwi.dslab.db.MysqlDao;
import com.kiwi.dslab.db.MysqlDaoImpl;
import com.kiwi.dslab.dto.db.OrderForm;
import com.kiwi.dslab.dto.db.OrderResponse;
import com.kiwi.dslab.zookeeper.ZkDao;
import com.kiwi.dslab.zookeeper.ZkDaoImpl;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.*;

import static com.kiwi.dslab.util.Utils.name2index;

public class MainProcess {


    public static void main(String[] args) {

        Gson gson = new Gson();
        MysqlDao mysqlDao = new MysqlDaoImpl();
        ZkDao zkDao = new ZkDaoImpl();


        SparkConf conf = new SparkConf().setAppName("spark-streaming").setMaster("local[*]");
        JavaStreamingContext streamingContext = new JavaStreamingContext(conf, Durations.seconds(1));

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", KafkaProperties.BROKER_LIST);
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", KafkaProperties.GROUP_ID);
//        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("auto.offset.reset", "earliest");
        kafkaParams.put("enable.auto.commit", false);

//        Collection<String> topics = Arrays.asList(KafkaProperties.TOPIC);
        Collection<String> topics = Arrays.asList("test");

        JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        streamingContext,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
                );

        stream.foreachRDD(record -> {
            record.foreach(r -> {
                System.out.println("Key:   " + r.key());
                System.out.println("Value: " + r.value());
//                OrderForm form = gson.fromJson(r.value(), OrderForm.class);
//
//                OrderResponse response = mysqlDao.buyItem(form);
//
//                if (!response.isSuccess()) {
//                    mysqlDao.storeResult(form.getUser_id(), form.getInitiator(), false, 0);
//                    return;
//                }
//
//                List<Double> exchangeRates = zkDao.getAllExchangeRate();
//                double paidInUnit = 0;
//                for (int i = 0; i < form.getItems().size(); i++) {
//                    paidInUnit += Integer.valueOf(form.getItems().get(i).getNumber())
//                            * response.getPrices().get(i)
//                            * exchangeRates.get(name2index.get(response.getCurrencies().get(i)));
//                }
//
//                double paidInCNY = paidInUnit / name2index.get("CNY");
//                mysqlDao.storeResult(form.getUser_id(), form.getInitiator(), true,
//                        paidInUnit / name2index.get(form.getInitiator()));
//                zkDao.increaseTotalTransactionBy(paidInCNY);
            });
        });

        streamingContext.start();
        try {
            streamingContext.awaitTermination();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            streamingContext.close();
        }
    }
}
