package org.pretty.http;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/2/24.
 */

public class ThreadPoolManger {
    private static ThreadPoolManger instance = new ThreadPoolManger();

    public static ThreadPoolManger getInstance() {
        return instance;
    }

    private int corePoolSize;//核心线程池的数量，同时能够执行的线程数量
    private int maximumPoolSize;//最大线程池数量，表示当缓冲队列满的时候能继续容纳的等待任务的数量
    private long keepAliveTime = 10;//存活时间  表示的是maximumPoolSize当中等待任务的存活时间
    private TimeUnit unit = TimeUnit.MINUTES;
    private ThreadPoolExecutor executor;

    private ThreadPoolManger() {

        // 给corePoolSize赋值：当前设备可用处理器核心数*2 + 1,能够让cpu的效率得到最大程度执行（有研究论证的）
        corePoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;
        maximumPoolSize = corePoolSize; //虽然maximumPoolSize用不到，但是需要赋值，否则报错
        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new LinkedBlockingQueue<Runnable>(),//缓冲队列，用于存放等待任务，Linked的先进先出
                Executors.defaultThreadFactory(), //创建线程的工厂
                new ThreadPoolExecutor.AbortPolicy()); //用来对超出maximumPoolSize的任务的处理策略
    }

    /**
     * 执行任务c
     */
    public void execute(Runnable runnable) {
        if (runnable == null) return;
        executor.execute(runnable);
    }

    /**
     * 从线程池中移除任务
     */
    public void remove(Runnable runnable) {
        if (runnable == null) return;
        executor.remove(runnable);
    }
}
