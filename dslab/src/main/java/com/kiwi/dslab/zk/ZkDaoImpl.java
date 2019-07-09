package com.kiwi.dslab.zk;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.kiwi.dslab.util.Utils.*;
import static com.kiwi.dslab.zk.PathConsts.*;

public class ZkDaoImpl implements ZkDao {

    private ZooKeeper zk = null;

    public ZkDaoImpl() {
        try {
            final CountDownLatch connectedSignal = new CountDownLatch(1);
            zk = new ZooKeeper("localhost:2181", 60000, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            });
            connectedSignal.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            zk = null;
        }
    }

    @Override
    public boolean increaseTotalTransactionBy(double amount) {
        if (zk == null) return false;
        Stat stat = new Stat();
        try {
            double ori_amount = Double.valueOf(new String(zk.getData(ZK_PATH_TXAMOUNT, false, stat)));
            zk.setData(ZK_PATH_TXAMOUNT, String.valueOf(ori_amount + amount).getBytes(), stat.getVersion());
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public double getTotalTransactionAmount() {
        return getDoubleByPath(ZK_PATH_TXAMOUNT);
    }

    @Override
    public List<Double> getAllExchangeRate() {
        if (zk == null) return null;
        List<Double> rates = new ArrayList<>();
        Stat stat = new Stat();

        try {
            rates.add(Double.valueOf(new String(zk.getData(ZK_PATH_CNY, false, stat))));
            rates.add(Double.valueOf(new String(zk.getData(ZK_PATH_USD, false, stat))));
            rates.add(Double.valueOf(new String(zk.getData(ZK_PATH_JPY, false, stat))));
            rates.add(Double.valueOf(new String(zk.getData(ZK_PATH_EUR, false, stat))));
            return rates;
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public double getExchangeRateByName(String currencyName) {
        String path = CURRENCY_PATHS.get(name2index.get(currencyName));
        return getDoubleByPath(path);
    }

    @Override
    public boolean putExchangeRateByName(String currencyName, double rate) {
        if (zk == null) return false;
        String path = CURRENCY_PATHS.get(name2index.get(currencyName));
        try {
            zk.setData(path, String.valueOf(rate).getBytes(), -1);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean changeExchangeRateRandomly(String currencyName) {
        double cur_rate = getExchangeRateByName(currencyName);
        if (cur_rate == -1) return false;
        double new_rate = cur_rate * ((getRandInt() % 20 + 90) / 100.0);
        return putExchangeRateByName(currencyName, new_rate);
    }

    @Override
    public ZooKeeper getZookeeper() {
        return zk;
    }

    @Override
    public void close() {
        if (zk != null) {
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private double getDoubleByPath(String path) {
        if (zk == null) return -1;
        Stat stat = new Stat();
        try {
            return Double.valueOf(new String(zk.getData(path, false, stat)));
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
