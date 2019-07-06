package com.kiwi.dslab.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Utils {
    public static final Map<String, Integer> name2index = new ConcurrentHashMap<>();
    public static final List<String> index2name = new ArrayList<>();
    private static Random random = new Random(0);

    static {
        name2index.put("CNY", 0);
        name2index.put("USD", 1);
        name2index.put("JPY", 2);
        name2index.put("EUR", 3);

        index2name.add("CNY");
        index2name.add("USD");
        index2name.add("JPY");
        index2name.add("EUR");
    }

    public static int getRandInt() {
        int num = random.nextInt();
        if (num < 0) return -num;
        return num;
//        int hash = 1315423911;
//        hash ^= ((hash << 5) + n + (hash >> 2));
//        if (hash < 0) hash = -hash;
//        return hash;
    }

}
