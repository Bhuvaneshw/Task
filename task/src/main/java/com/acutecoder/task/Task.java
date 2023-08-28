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

    private final TaskRunnable<T> taskRunnable;
    private TaskResult<T> taskResult;
    private TaskError taskError;
    private TaskCallback onStart, onEnd;
    private boolean isBackground = true, isRunning = false;

    /**
     * Creates a new Task instance
     *
     * @param taskRunnable The task
     */
    public Task(TaskRunnable<T> taskRunnable) {
        this.taskRunnable = taskRunnable;
    }

    /**
     * Creates a new Task instance
     *
     * @param taskRunnable The task
     * @return Task instance
     */
    public static Task<?> with(TaskRunnable<?> taskRunnable) {
        return new Task<>(taskRunnable);
    }

    private static Handler getForegroundHandler() {
        return new Handler(Looper.getMainLooper());
    }

    /**
     * Called when the task is successfully completed without error
     */
    public Task<T> onResult(TaskResult<T> taskResult) {
        this.taskResult = taskResult;
        return this;
    }

    /**
     * Called when the task is failed
     */
    public Task<T> onError(TaskError taskError) {
        this.taskError = taskError;
        return this;
    }

    /**
     * Called before the execution of task
     */
    public Task<T> onStart(TaskCallback onStart) {
        this.onStart = onStart;
        return this;
    }

    /**
     * Called after the execution of task regardless the completion of the task (Whether the task is executed without error)
     */
    public Task<T> onEnd(TaskCallback onEnd) {
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
                                if (onStart != null) onStart.run();
                                T o = taskRunnable.run();
                                if (taskResult != null) {
                                    taskResult.onResult(o);
                                }
                                isRunning = false;
                                if (onEnd != null) onEnd.run();
                            } catch (Exception e) {
                                isRunning = false;
                                if (onEnd != null) onEnd.run();
                                if (taskError != null) Task.this.onError(e);
                                else throw new TaskException(e, isBackground);
                            }
                        }
                    });
                } catch (Exception e) {
                    if (taskError != null) onError(e);
                    else throw new TaskException(e, isBackground);
                }
            } else {
                try {
                    if (onStart != null) onStart.run();
                    new Thread(new java.lang.Runnable() {
                        @Override
                        public void run() {
                            try {
                                assert taskRunnable != null;
                                final T o = taskRunnable.run();
                                getForegroundHandler().post(new java.lang.Runnable() {
                                    @Override
                                    public void run() {
                                        if (taskResult != null) taskResult.onResult(o);
                                        if (onEnd != null) onEnd.run();
                                    }
                                });
                                isRunning = false;
                            } catch (Exception e) {
                                isRunning = false;
                                if (onEnd != null)
                                    getForegroundHandler().post(new java.lang.Runnable() {
                                        @Override
                                        public void run() {
                                            onEnd.run();
                                        }
                                    });
                                if (taskError != null) Task.this.onError(e);
                                else throw new TaskException(e, isBackground);
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    if (taskError != null) onError(e);
                    else throw new TaskException(e, isBackground);
                }
            }
        } catch (Exception e) {
            if (taskError != null) onError(e);
            else throw new TaskException(e, isBackground);
        }
    }

    private void onError(final Exception e) {
        getForegroundHandler().post(new java.lang.Runnable() {
            @Override
            public void run() {
                taskError.onError(e);
            }
        });
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
}
