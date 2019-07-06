package com.kiwi.dslab.zookeeper;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

public class LockTest {
    public static void main(String[] args) throws Exception {
        String host = args[0];
        String path = args[1];
        String myName = args[2];
        final CountDownLatch connectedSignal = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper(host, 5000, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectedSignal.countDown();
            }
        });
        connectedSignal.await();

        DistributedLock lock = new DistributedLock(myName, zk, path);

        lock.lock();
        xixixi();
        lock.unlock();

    }

    private static void xixixi() {

    }
}
