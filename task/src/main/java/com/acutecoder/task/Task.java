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
    private OnProgress onProgress;
    private NextTask<T> next;

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
     * Called to update progress (in UI) by publishProgress function in UI Thread
     */
    public Task<T> onProgress(OnProgress onProgress) {
        this.onProgress = onProgress;
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
            if (isBackground) {
                try {
                    if (onStart != null) onStart.run();
                    new Thread(new java.lang.Runnable() {
                        @Override
                        public void run() {
                            try {
                                assert taskRunnable != null;
                                final T o = taskRunnable.run(Task.this);
                                getForegroundHandler().post(new java.lang.Runnable() {
                                    @Override
                                    public void run() {
                                        if (taskResult != null) taskResult.onResult(o);
                                        if (onEnd != null) onEnd.run();
                                        if (next != null) next.run(o).start();
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
            } else {
                try {
                    getForegroundHandler().post(new java.lang.Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (onStart != null) onStart.run();
                                T o = taskRunnable.run(Task.this);
                                if (taskResult != null) {
                                    taskResult.onResult(o);
                                }
                                isRunning = false;
                                if (onEnd != null) onEnd.run();
                                if (next != null) next.run(o).start();
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
            }
        } catch (Exception e) {
            if (taskError != null) onError(e);
            else throw new TaskException(e, isBackground);
        }
    }

    /**
     * Publish the progress in UI Thread
     */
    public Task<T> publishProgress(final Object... progress) {
        if (onProgress != null)
            getForegroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    onProgress.progress(progress);
                }
            });
        return this;
    }

    /**
     * Sleeps thread for given milliseconds
     */
    public Task<T> sleep(long mills) throws InterruptedException {
        Thread.sleep(mills);
        return this;
    }

    /**
     * Chains next Task
     */
    public Task<T> then(NextTask<T> nextTask) {
        next = nextTask;
        return this;
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
        public static void start(java.lang.Runnable runnable) {
            getForegroundHandler().post(runnable);
        }
    }

    public static class Background {
        /**
         * Runs a simple task in new thread
         */
        public static void start(java.lang.Runnable runnable) {
            new Thread(runnable).start();
        }
    }
}
