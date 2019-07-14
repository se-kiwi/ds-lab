package com.kiwi.dslab;

import com.google.gson.Gson;
import com.kiwi.dslab.db.MysqlDao;
import com.kiwi.dslab.db.MysqlDaoImpl;
import com.kiwi.dslab.dto.OrderForm;
import com.kiwi.dslab.dto.OrderResponse;
import com.kiwi.dslab.zk.DistributedLock;
import com.kiwi.dslab.zk.ZkDao;
import com.kiwi.dslab.zk.ZkDaoImpl;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.PropertyConfigurator;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

import java.util.*;

import static com.kiwi.dslab.util.Utils.name2index;

public class MainProcess {
    private static final Gson gson = new Gson();
    private static final Logger LOG = LoggerFactory.getLogger(MainProcess.class);

    public static void main(String[] args) {
        System.out.println(">>>>>>>> BEGIN <<<<<<<<");
        PropertyConfigurator.configure(ClusterConf.LOG_PROPERTY);
        SparkConf conf = new SparkConf().setAppName("spark-app").setMaster(ClusterConf.MASTER);
        JavaStreamingContext streamingContext = new JavaStreamingContext(conf, Durations.seconds(1));
//        streamingContext.

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", ClusterConf.BROKER_LIST);
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", ClusterConf.GROUP_ID);
//        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("auto.offset.reset", "earliest");
        kafkaParams.put("enable.auto.commit", false);

        Collection<String> topics = Arrays.asList(ClusterConf.TOPIC);
//        Collection<String> topics = Arrays.asList("test006");

        JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        streamingContext,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
                );

        stream.foreachRDD(record -> {
            record.foreach(r -> {
                LOG.info("Key:   " + r.key());
                LOG.info("Value: " + r.value());
                MysqlDao mysqlDao = new MysqlDaoImpl();
                ZkDao zkDao = new ZkDaoImpl();
                OrderForm form = gson.fromJson(r.value(), OrderForm.class);
                form.setOrder_id(r.key());
                DistributedLock lock = new DistributedLock(zkDao.getZookeeper());


                OrderResponse response;
                try {
                    LOG.warn("[" + zkDao.getZookeeper().getSessionId() + "]: try to lock");
                    lock.lock();
                    LOG.warn("[" + zkDao.getZookeeper().getSessionId() + "]: get lock");
                    response = mysqlDao.buyItem(form, zkDao.getZookeeper());
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                    return;
                } finally {
                    lock.unlock();
                    LOG.warn("[" + zkDao.getZookeeper().getSessionId() + "]: release lock");
                }

                if (!response.isSuccess()) {
                    mysqlDao.storeResult(form.getOrder_id(), form.getUser_id(), form.getInitiator(), false, 0);
                    zkDao.close();
                    return;
                }

                if (form.getItems().size() != response.getCurrencies().size()) {
//                    System.out.printf("currency: %d, actual: %d\n", response.getCurrencies().size(), form.getItems().size());
                    LOG.error("currency: " + response.getCurrencies() + ", actual: " + form.getItems());
                }
                if (form.getItems().size() != response.getPrices().size()) {
                    LOG.error("price: " + response.getPrices() + ", actual: " + form.getItems());
//                    System.out.printf("price: %d, actual: %d\n", response.getPrices().size(), form.getItems().size());
                }


                List<Double> exchangeRates = zkDao.getAllExchangeRate();
                double paidInUnit = 0;
                for (int i = 0; i < form.getItems().size(); i++) {
                    paidInUnit += Integer.valueOf(form.getItems().get(i).getNumber())
                            * response.getPrices().get(i)
                            * exchangeRates.get(name2index.get(response.getCurrencies().get(i)));
                }

                double paidInCNY = paidInUnit / exchangeRates.get(name2index.get("CNY"));
                mysqlDao.storeResult(form.getOrder_id(), form.getUser_id(), form.getInitiator(), true,
                        paidInUnit / exchangeRates.get(name2index.get(form.getInitiator())));
                zkDao.increaseTotalTransactionBy(paidInCNY);
                zkDao.close();
            });
        });

        stream.count();

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
