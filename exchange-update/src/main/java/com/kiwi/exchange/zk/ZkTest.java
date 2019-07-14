package com.kiwi.exchange.zk;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

import static com.kiwi.exchange.ClusterConf.ZK;

public class ZkTest {
    public static void main(String[] args) throws Exception {
        final CountDownLatch connectedSignal = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper(ZK, 5000, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectedSignal.countDown();
            }
        });
        connectedSignal.await();

//        com.kiwi.exchange.zk.create("/test000", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//
//        com.kiwi.exchange.zk.create("/kiwi/CNY", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//        com.kiwi.exchange.zk.create("/kiwi/USD", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//        com.kiwi.exchange.zk.create("/kiwi/JPY", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//        com.kiwi.exchange.zk.create("/kiwi/EUR", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

//        com.kiwi.exchange.zk.create("/kiwi/txAmount", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

//        com.kiwi.exchange.zk.setData("/kiwi/CNY", "0.4".getBytes(), -1);
//        com.kiwi.exchange.zk.setData("/kiwi/USD", "0.7".getBytes(), -1);
//        com.kiwi.exchange.zk.setData("/kiwi/JPY", "1.0".getBytes(), -1);
//        com.kiwi.exchange.zk.setData("/kiwi/EUR", "3.0".getBytes(), -1);

//        com.kiwi.exchange.zk.setData("/test000", "0".getBytes(), -1);

//        List<String> zooChildren = com.kiwi.exchange.zk.getChildren("/kiwi", false);
//        System.out.println(zooChildren);

        Stat stat = new Stat();
//        System.out.println(new String(com.kiwi.exchange.zk.getData("/brokers/topics/yfzm/partitions/0/state", false, stat)));
//        System.out.println(new String(com.kiwi.exchange.zk.getData("/test000", false, stat)));
//        System.out.println(stat.getVersion());

        zk.setData("/kiwi/txAmount", "0".getBytes(), -1);
        System.out.println(new String(zk.getData("/kiwi/txAmount", false, stat)));
        System.out.println(stat.getVersion());

//        for (String child : zooChildren) {
//            System.out.println(child);
//        }

        zk.close();
    }
}
