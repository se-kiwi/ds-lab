package com.kiwi.dslab;

import com.kiwi.dslab.zookeeper.ZkDaoImpl;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExchangeSchedule {
    private static ZkDaoImpl zDao;

    private static Runnable RMBRunner = () -> {
        System.out.println("RMB round:" + new Date());
        zDao.changeExchangeRateRandomly("RMB");
    };
    private static Runnable USDRunner = () -> {
        System.out.println("USD round:" + new Date());
        zDao.changeExchangeRateRandomly("USD");
    };
    private static Runnable JPYRunner = () -> {
        System.out.println("JPY round:" + new Date());
        zDao.changeExchangeRateRandomly("JYP");
    };
    private static Runnable EURRunner = () -> {
        System.out.println("EUR round:" + new Date());
        zDao.changeExchangeRateRandomly("EUR");
    };


    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorService =  Executors.newScheduledThreadPool(5);
        scheduledExecutorService.scheduleWithFixedDelay(RMBRunner,5,60,TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(USDRunner,5, 60, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(JPYRunner,5, 60, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(EURRunner,5, 60, TimeUnit.SECONDS);
    }
}
