package com.kiwi.dslab.db;

import com.kiwi.dslab.dto.OrderForm;
import com.kiwi.dslab.dto.OrderResponse;
import com.kiwi.dslab.dto.ResultResponse;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

public interface MysqlDao {
    OrderResponse buyItem(OrderForm order, ZooKeeper zooKeeper);
    boolean storeResult(String order_id, String user_id, String initiator, boolean success, double paid);

    boolean initCommodity();
    boolean initResult();
}
