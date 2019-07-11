package com.kiwi.httpserver.mysql;

import java.util.List;

public interface MysqlDao {
    ResultResponse getResultById(String id);

    List<ResultResponse> getResultByUserId(String userId);
}
