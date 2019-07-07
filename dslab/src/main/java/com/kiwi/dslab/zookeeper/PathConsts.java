package com.kiwi.dslab.zookeeper;

import java.util.ArrayList;
import java.util.List;

public class PathConsts {
    public static final String ZK_PATH_ROOT = "/kiwi";
    public static final String ZK_PATH_CNY = "/kiwi/CNY";
    public static final String ZK_PATH_USD = "/kiwi/USD";
    public static final String ZK_PATH_JPY = "/kiwi/JPY";
    public static final String ZK_PATH_EUR = "/kiwi/EUR";
    public static final String ZK_PATH_TXAMOUNT = "/kiwi/txAmount";

    public static final List<String> CURRENCY_PATHS = new ArrayList<>();

    static {
        CURRENCY_PATHS.add(ZK_PATH_CNY);
        CURRENCY_PATHS.add(ZK_PATH_USD);
        CURRENCY_PATHS.add(ZK_PATH_JPY);
        CURRENCY_PATHS.add(ZK_PATH_EUR);
    }
}
