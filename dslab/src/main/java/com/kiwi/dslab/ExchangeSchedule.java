package com.kiwi.dslab;

import com.kiwi.dslab.zookeeper.ZkDaoImpl;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExchangeSchedule {
//    private static ZkDaoImpl zDao = new ZkDaoImpl();

    private static Runnable CNYRunner = () -> {
        System.out.println("CNY round:" + new Date());
        ZkDaoImpl zDao = new ZkDaoImpl();
        zDao.changeExchangeRateRandomly("CNY");
        zDao.close();
    };
    private static Runnable USDRunner = () -> {
        System.out.println("USD round:" + new Date());
        ZkDaoImpl zDao = new ZkDaoImpl();
        zDao.changeExchangeRateRandomly("USD");
        zDao.close();

    };
    private static Runnable JPYRunner = () -> {
        System.out.println("JPY round:" + new Date());
        ZkDaoImpl zDao = new ZkDaoImpl();
        zDao.changeExchangeRateRandomly("JYP");
        zDao.close();
    };
    private static Runnable EURRunner = () -> {
        System.out.println("EUR round:" + new Date());
        ZkDaoImpl zDao = new ZkDaoImpl();
        zDao.changeExchangeRateRandomly("EUR");
        zDao.close();
    };


    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorService =  Executors.newScheduledThreadPool(5);
        scheduledExecutorService.scheduleWithFixedDelay(CNYRunner,5,60,TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(USDRunner,5, 60, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(JPYRunner,5, 60, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(EURRunner,5, 60, TimeUnit.SECONDS);
    }
}
