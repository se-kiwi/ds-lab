package com.kiwi.dslab;

public final class ClusterConf {
//    public static final String MASTER = "k8s://https://10.0.0.81:6443";
//    public static final String ZK = "kafkatest-zookeeper:2181";
//    public static final String BROKER_LIST = "kafkatest:9092";
//    public static final String MYSQL_PATH = "jdbc:mysql://10.0.0.22:30706/dslab";

    public static final String MASTER = "local[*]";
    public static final String ZK = "localhost:2181";
    public static final String BROKER_LIST = "localhost:9092";
    public static final String MYSQL_PATH = "jdbc:mysql://202.120.40.8:30706/dslab";

    // Kafka Conf
    public static final String TOPIC = "test009";
    public static final String GROUP_ID = "group01";

    // MySQL Conf
    public static final String MYSQL_USER = "root";
    public static final String MYSQL_PASSWD = "root";
}
