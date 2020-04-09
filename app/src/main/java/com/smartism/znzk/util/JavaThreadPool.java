package com.smartism.znzk.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaThreadPool {
    private static final int THREADS = 10;
    private static JavaThreadPool instance;
    private ExecutorService workerExecutor;

    private JavaThreadPool() {
        workerExecutor = Executors.newFixedThreadPool(THREADS);
    }

    public synchronized static JavaThreadPool getInstance() {
        if (instance == null) {
            instance = new JavaThreadPool();
        }
        return instance;
    }

    /**
     * 将线程放入池中
     *
     * @param runnable
     */
    public void excute(Runnable runnable) {
        if (workerExecutor != null) {
            workerExecutor.execute(runnable);
        }
    }

    /**
     * 关闭线程池
     */
    public void shutDownPool() {
        if (workerExecutor != null) {
            workerExecutor.shutdown();
        }
    }

}
