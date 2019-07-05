package com.kiwi.dslab.db;

import com.kiwi.dslab.dto.db.OrderForm;
import com.kiwi.dslab.dto.db.OrderResponse;

import java.util.List;

public interface MysqlDao {
    boolean buyItem(OrderForm order);
    boolean storeResult(String id, String user_id, String initiator, boolean success, double paid);
    OrderResponse getResultById(String id);
    List<OrderResponse> getResultByUserId(String userId);
}
