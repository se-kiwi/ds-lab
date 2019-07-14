package com.kiwi.exchange.zk;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.recipes.lock.LockListener;
import org.apache.zookeeper.recipes.lock.WriteLock;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DistributedLock {
    private WriteLock writeLock;
    private CountDownLatch lockAcquiredSignal = new CountDownLatch(1);

    private static final List<ACL> DEFAULT_ACL = ZooDefs.Ids.OPEN_ACL_UNSAFE;

    public DistributedLock(ZooKeeper zooKeeper) {
        this("commodity lock", zooKeeper, "/org/apache/zookeeper/recipes/lock", DEFAULT_ACL);
    }

    public DistributedLock(String name, ZooKeeper zooKeeper, String path) {
        this(name, zooKeeper, path, DEFAULT_ACL);
    }

    public DistributedLock(String name, ZooKeeper zooKeeper, String path, List<ACL> acl) {
        writeLock = new WriteLock(zooKeeper, path, acl, new SyncLockListener());
    }

    public void lock() throws KeeperException, InterruptedException {
        writeLock.lock();
        lockAcquiredSignal.await();
    }

    public boolean lock(long timeout, TimeUnit timeUnit) throws KeeperException, InterruptedException {
        writeLock.lock();
        return lockAcquiredSignal.await(timeout, timeUnit);
    }

    public boolean tryLock() throws KeeperException, InterruptedException {
        return lock(1, TimeUnit.SECONDS);
    }

    public void unlock() {
        writeLock.unlock();
    }

    private class SyncLockListener implements LockListener {
        @Override
        public void lockAcquired() {
            lockAcquiredSignal.countDown();
        }

        @Override
        public void lockReleased() {

        }
    }
}
