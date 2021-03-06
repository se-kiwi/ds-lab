package com.kiwi.httpserver.config;

public final class Conf {
    // nanohttpd
    public static final int SERVER_PORT = 30623;

//    public static final String ZK = "kafkatest-zookeeper:2181";
//    public static final String BROKER_LIST = "kafkatest:9092";
//    public static final String MYSQL_PATH = "jdbc:mysql://10.0.0.22:30706/dslab";


    public static final String ZK = "localhost:2181";
    public static final String BROKER_LIST = "localhost:9092";
    public static final String MYSQL_PATH = "jdbc:mysql://202.120.40.8:30706/dslab";

    // Kafka Conf
    public static String TOPIC = "test015";
    public static final String GROUP_ID = "group01";


    // MySQL Conf
    public static final String MYSQL_USER = "root";
    public static String MYSQL_PASSWD = "root";

    // Zookeeper Conf
    public static final String ZK_PATH_TXAMOUNT = "/kiwi/txAmount";
    public static final int ZK_TIMEOUT = 60000;

}
