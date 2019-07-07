package com.kiwi.dslab.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZkTest {
    public static void main(String[] args) throws Exception {
        final CountDownLatch connectedSignal = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper("localhost:2181", 5000, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectedSignal.countDown();
            }
        });
        connectedSignal.await();

//        zk.create("/test000", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//
//        zk.create("/kiwi/CNY", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//        zk.create("/kiwi/USD", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//        zk.create("/kiwi/JPY", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//        zk.create("/kiwi/EUR", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

//        zk.create("/kiwi/txAmount", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        zk.setData("/kiwi/CNY", "0.4".getBytes(), -1);
        zk.setData("/kiwi/USD", "0.7".getBytes(), -1);
        zk.setData("/kiwi/JPY", "1.0".getBytes(), -1);
        zk.setData("/kiwi/EUR", "3.0".getBytes(), -1);

//        zk.setData("/test000", "0".getBytes(), -1);

        List<String> zooChildren = zk.getChildren("/kiwi", false);
        System.out.println(zooChildren);

        Stat stat = new Stat();
//        System.out.println(new String(zk.getData("/brokers/topics/yfzm/partitions/0/state", false, stat)));
        System.out.println(new String(zk.getData("/test000", false, stat)));
        System.out.println(stat.getVersion());

//        zk.setData("/kiwi/txAmount", "3.55".getBytes(), stat.getVersion());
//        System.out.println(new String(zk.getData("/kiwi/txAmount", false, stat)));
//        System.out.println(stat.getVersion());

//        for (String child : zooChildren) {
//            System.out.println(child);
//        }

        zk.close();
    }
}
