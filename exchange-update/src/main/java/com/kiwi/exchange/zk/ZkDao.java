package com.kiwi.exchange.zk;

import org.apache.zookeeper.ZooKeeper;

import java.util.List;

public interface ZkDao {
    boolean increaseTotalTransactionBy(double amount);
    double getTotalTransactionAmount();

    List<Double> getAllExchangeRate();  // CNY, USD, JPY, EUR
    double getExchangeRateByName(String currencyName);
    boolean putExchangeRateByName(String currencyName, double rate);
    boolean changeExchangeRateRandomly(String currencyName);

    ZooKeeper getZookeeper();

    void close();
}
