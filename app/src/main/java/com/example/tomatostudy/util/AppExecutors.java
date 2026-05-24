package com.example.tomatostudy.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppExecutors {

    private static final ExecutorService IO_EXECUTOR = Executors.newFixedThreadPool(2);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public interface Callback<T> {
        void onComplete(T result);
    }

    public static void executeOnIo(Runnable runnable) {
        IO_EXECUTOR.execute(runnable);
    }

    public static void postToMain(Runnable runnable) {
        MAIN_HANDLER.post(runnable);
    }

    private AppExecutors() {
    }
}
