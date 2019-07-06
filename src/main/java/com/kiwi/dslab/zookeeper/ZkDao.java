package com.kiwi.dslab.zookeeper;

public interface ZkDao {
    boolean increaseTotalTransactionBy(double amount);
    double getTotalTransactionAmount();

    double getExchangeRateByName(String currencyName);
    boolean putExchangeRateByName(String currencyName);
    boolean changeExchangeRateRandomly(String currencyName);
}
