package com.kiwi.httpserver.zookeeper;

import java.util.List;

public interface ZkDao {

    void close();

    Double getTotalTransactionAmount();
}
