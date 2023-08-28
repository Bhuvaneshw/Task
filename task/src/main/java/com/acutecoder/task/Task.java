package com.acutecoder.task;

import android.os.Handler;
import android.os.Looper;

/**
 * Runs a task in Background thread or Foreground thread (UI thread)
 * Created by Bhuvaneshwaran
 * on 11:00 PM, 4/10/2023
 *
 * @author AcuteCoder
 */

public class Task<T> {

    private final Runnable<T> runnable;
    private Result<T> result;
    private Error error;
    private Callback onStart, onEnd;
    private boolean isBackground = true, isRunning = false;

    /**
     * Creates a new Task instance
     *
     * @param runnable The task
     */
    public Task(Runnable<T> runnable) {
        this.runnable = runnable;
    }

    /**
     * Creates a new Task instance
     *
     * @param runnable The task
     * @return Task instance
     */
    public static Task<?> with(Runnable<?> runnable) {
        return new Task<>(runnable);
    }

    private static Handler getForegroundHandler() {
        return new Handler(Looper.getMainLooper());
    }

    /**
     * Called when the task is successfully completed without error
     */
    public Task<T> onResult(Result<T> result) {
        this.result = result;
        return this;
    }

    /**
     * Called when the task is failed
     */
    public Task<T> onError(Error error) {
        this.error = error;
        return this;
    }

    /**
     * Called before the execution of task
     */
    public Task<T> onStart(Callback onStart) {
        this.onStart = onStart;
        return this;
    }

    /**
     * Called after the execution of task regardless the completion of the task (Whether the task is executed without error)
     */
    public Task<T> onEnd(Callback onEnd) {
        this.onEnd = onEnd;
        return this;
    }

    /**
     * Runs the task in background thread
     */
    public Task<T> doInBackground() {
        isBackground = true;
        return this;
    }

    /**
     * Runs the task in foreground thread (Main thread or UI thread)
     */
    public Task<T> doInForeground() {
        isBackground = false;
        return this;
    }

    /**
     * Starts the execution of the task
     */
    public void start() {
        try {
            if (isRunning) throw new TaskException("Task already running!", isBackground);
            isRunning = true;
            if (!isBackground) {
                try {
                    getForegroundHandler().post(new java.lang.Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (onStart != null) onStart.callback();
                                T o = runnable.run();
                                if (result != null) {
                                    result.onResult(o);
                                }
                                isRunning = false;
                                if (onEnd != null) onEnd.callback();
                            } catch (Exception e) {
                                isRunning = false;
                                if (onEnd != null) onEnd.callback();
                                if (error != null) Task.this.onError(e);
                                else throw new TaskException(e, isBackground);
                            }
                        }
                    });
                } catch (Exception e) {
                    if (error != null) onError(e);
                    else throw new TaskException(e, isBackground);
                }
            } else {
                try {
                    if (onStart != null) onStart.callback();
                    new Thread(new java.lang.Runnable() {
                        @Override
                        public void run() {
                            try {
                                assert runnable != null;
                                final T o = runnable.run();
                                getForegroundHandler().post(new java.lang.Runnable() {
                                    @Override
                                    public void run() {
                                        if (result != null) result.onResult(o);
                                        if (onEnd != null) onEnd.callback();
                                    }
                                });
                                isRunning = false;
                            } catch (Exception e) {
                                isRunning = false;
                                if (onEnd != null)
                                    getForegroundHandler().post(new java.lang.Runnable() {
                                        @Override
                                        public void run() {
                                            onEnd.callback();
                                        }
                                    });
                                if (error != null) Task.this.onError(e);
                                else throw new TaskException(e, isBackground);
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    if (error != null) onError(e);
                    else throw new TaskException(e, isBackground);
                }
            }
        } catch (Exception e) {
            if (error != null) onError(e);
            else throw new TaskException(e, isBackground);
        }
    }

    private void onError(final Exception e) {
        getForegroundHandler().post(new java.lang.Runnable() {
            @Override
            public void run() {
                error.onError(e);
            }
        });
    }

    public interface Runnable<T> {
        T run() throws Exception;
    }

    public interface Result<T> {
        void onResult(T result);
    }

    public interface Error {
        void onError(Exception e);
    }

    public interface Callback {
        void callback();
    }

    public static class Foreground {
        /**
         * Runs a simple task in UI thread
         */
        public static void run(java.lang.Runnable runnable) {
            getForegroundHandler().post(runnable);
        }
    }

    public static class Background {
        /**
         * Runs a simple task in new thread
         */
        public static void run(java.lang.Runnable runnable) {
            new Thread(runnable).start();
        }
    }

    public static class TaskException extends RuntimeException {
        public TaskException(Exception e, boolean isBackground) {
            super("Error while executing " + (isBackground ? "Background" : "Foreground") + " task", e);
        }

        public TaskException(String msg, boolean isBackground) {
            super("Error while executing " + (isBackground ? "Background" : "Foreground") + " task: " + msg);
        }
    }
}
