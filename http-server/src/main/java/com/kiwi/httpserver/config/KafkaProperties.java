package com.kiwi.httpserver.config;

public final class KafkaProperties {
    public static final String ZK="localhost:2181";      //Zookeeper地址
    public static final String TOPIC="test";                   //topic名称
    public static final String BROKER_LIST="localhost:9092";    //Broker列表
    public static final String GROUP_ID="group01";              //消费者使用
}
