package com.kiwi.dslab.db;

import com.kiwi.dslab.dto.db.OrderForm;
import com.kiwi.dslab.dto.db.OrderResponse;
import com.kiwi.dslab.dto.db.ResultResponse;

import java.util.List;

public interface MysqlDao {
    OrderResponse buyItem(OrderForm order);
    boolean storeResult(String user_id, String initiator, boolean success, double paid);
    ResultResponse getResultById(String id);
    List<ResultResponse> getResultByUserId(String userId);

    boolean initCommodity();
}
