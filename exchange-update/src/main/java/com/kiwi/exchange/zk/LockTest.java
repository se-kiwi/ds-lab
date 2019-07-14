package com.kiwi.exchange.zk;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

import static com.kiwi.exchange.ClusterConf.ZK;

public class LockTest {

    public static void main(String[] args) throws Exception {
        String host = ZK;
        String path = "/org/apache/zookeeper/recipes/lock";
        final CountDownLatch connectedSignal = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper(host, 5000, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectedSignal.countDown();
            }
        });
        connectedSignal.await();

        new Thread(() -> {
            DistributedLock lock = new DistributedLock("lock1", zk, "/org/apache/zookeeper/recipes/lock");
            try {
                lock.lock();
                for (int i = 0; i < 10; i++) {
                    System.out.println("111111111111");
                    Thread.sleep(100);
                }
                lock.unlock();
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            DistributedLock lock = new DistributedLock("lock2", zk, "/org/apache/zookeeper/recipes/lock");
            try {
                lock.lock();
                for (int i = 0; i < 10; i++) {
                    System.out.println("222222222222");
                    Thread.sleep(100);
                }
                lock.unlock();
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

//        DistributedLock lock = new DistributedLock("test", com.kiwi.exchange.zk, path);
//
//        lock.lock();
//        xixixi();
//        lock.unlock();

    }

    private static void xixixi() {
        System.out.println("xixixi");
    }
}
