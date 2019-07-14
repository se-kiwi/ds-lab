package com.kiwi.dslab;

import com.google.gson.Gson;
import com.kiwi.dslab.db.MysqlDao;
import com.kiwi.dslab.db.MysqlDaoImpl;
import com.kiwi.dslab.dto.Item;
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

import java.time.Duration;
import java.time.LocalDate;
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
                long rdd_start = System.nanoTime();
                LOG.info("Key:   " + r.key());
                LOG.info("Value: " + r.value());
                MysqlDao mysqlDao = new MysqlDaoImpl();
                ZkDao zkDao = new ZkDaoImpl();
                OrderForm form = gson.fromJson(r.value(), OrderForm.class);
                form.setOrder_id(r.key());
                List<Item> sorted = form.getItems();
                sorted.sort(Comparator.comparing(Item::getId));
                List<DistributedLock> lockList = new ArrayList<>();
                for (Item i : sorted){
                    DistributedLock lock = new DistributedLock(zkDao.getZookeeper(),i.getId());
                    lockList.add(lock);
                }
                OrderResponse response = new OrderResponse();
              
                try {
                    long l_start = System.nanoTime();
                    for (DistributedLock lc : lockList) {
                        lc.lock();
                    }
                    long l_end = System.nanoTime();
                    System.out.println("[locking] time cost: " +String.valueOf(l_end-l_start));
                    System.out.println("before get response");
                    long r_start = System.nanoTime();
                    response = mysqlDao.buyItem(form, zkDao.getZookeeper());
                    long r_end = System.nanoTime();
                    System.out.println("after get response");
                    System.out.println("[buyItem] time cost: " +String.valueOf(r_end-r_start));
                }catch (KeeperException | InterruptedException e){
                    e.printStackTrace();
                    return;
                }finally {
                    Collections.reverse(lockList);
                    for (DistributedLock lc : lockList) {
                        lc.unlock();
                    }

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

                long ex_start = System.nanoTime();
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
                long rdd_end = System.nanoTime();
                System.out.println("[read exchange] time cost: " +String.valueOf(rdd_end-ex_start));
                System.out.println("[RDD] time cost: " +String.valueOf(rdd_end-rdd_start));

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
