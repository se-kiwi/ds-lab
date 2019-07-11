package com.kiwi.httpserver.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static com.kiwi.httpserver.config.Conf.*;

public class ZkDaoImpl implements ZkDao {

    private ZooKeeper zk = null;

    public ZkDaoImpl() {
        try {
            final CountDownLatch connectedSignal = new CountDownLatch(1);
            zk = new ZooKeeper(ZK, ZK_TIMEOUT, event -> {
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
    public Double getTotalTransactionAmount() {
        return getDoubleByPath(ZK_PATH_TXAMOUNT);
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
}
