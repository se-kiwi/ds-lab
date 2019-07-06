package com.kiwi.dslab.zookeeper;

import java.util.List;

public interface ZkDao {
    boolean increaseTotalTransactionBy(double amount);
    double getTotalTransactionAmount();

    List<Double> getAllExchangeRate();  // CNY, USD, JPY, EUR
    double getExchangeRateByName(String currencyName);
    boolean putExchangeRateByName(String currencyName);
    boolean changeExchangeRateRandomly(String currencyName);
}
