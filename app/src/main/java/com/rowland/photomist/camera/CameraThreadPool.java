package com.rowland.photomist.camera;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rowland on 4/20/2017.
 */

public class CameraThreadPool {
    private static final int KEEP_ALIVE = 10;
    private static CameraThreadPool mInstance;
    private static int MAX_POOL_SIZE;
    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    private ThreadPoolExecutor mThreadPoolExec;

    private CameraThreadPool() {
        int coreNum = Runtime.getRuntime().availableProcessors();
        MAX_POOL_SIZE = coreNum * 2;
        mThreadPoolExec = new ThreadPoolExecutor(
                coreNum,
                MAX_POOL_SIZE,
                KEEP_ALIVE,
                TimeUnit.SECONDS,
                workQueue);
    }

    public static synchronized void post(Runnable runnable) {
        if (mInstance == null) {
            mInstance = new CameraThreadPool();
        }
        mInstance.mThreadPoolExec.execute(runnable);
    }

    public static void finish() {
        mInstance.mThreadPoolExec.shutdown();
    }
}
