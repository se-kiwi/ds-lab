package com.kiwi.dslab.zookeeper;

public class ZkDaoImpl implements ZkDao {
    @Override
    public boolean increaseTotalTransactionBy(double amount) {
        return false;
    }

    @Override
    public double getTotalTransactionAmount() {
        return 0;
    }

    @Override
    public double getExchangeRateByName(String currencyName) {
        return 0;
    }

    @Override
    public boolean putExchangeRateByName(String currencyName) {
        return false;
    }

    @Override
    public boolean changeExchangeRateRandomly(String currencyName) {
        return false;
    }
}
