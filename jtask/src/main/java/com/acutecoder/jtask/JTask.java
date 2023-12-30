package com.acutecoder.jtask;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by Bhuvaneshwaran
 * on 11:00 PM, 4/10/2023
 *
 * @author AcuteCoder
 */

public class JTask<T> {

    private final JTaskRunnable<T> taskRunnable;
    private JTaskResult<T> taskResult;
    private JTaskError taskError;
    private JTaskCallback onStart, onEnd, onCancel;
    private boolean isBackground = true, isRunning = false, isCancelled = false;
    private OnProgress onProgress;
    private NextJTask<T> next;

    public JTask(JTaskRunnable<T> taskRunnable) {
        this.taskRunnable = taskRunnable;
    }

    public static JTask<?> with(JTaskRunnable<?> taskRunnable) {
        return new JTask<>(taskRunnable);
    }

    private static Handler getForegroundHandler() {
        return new Handler(Looper.getMainLooper());
    }

    public JTask<T> onStart(JTaskCallback onStart) {
        this.onStart = onStart;
        return this;
    }

    public JTask<T> onEnd(JTaskCallback onEnd) {
        this.onEnd = onEnd;
        return this;
    }

    public JTask<T> onResult(JTaskResult<T> taskResult) {
        this.taskResult = taskResult;
        return this;
    }

    public JTask<T> onError(JTaskError taskError) {
        this.taskError = taskError;
        return this;
    }

    public JTask<T> onCancel(JTaskCallback onCancel) {
        this.onCancel = onCancel;
        return this;
    }

    public JTask<T> onProgress(OnProgress onProgress) {
        this.onProgress = onProgress;
        return this;
    }

    public JTask<T> doInBackground() {
        isBackground = true;
        return this;
    }

    public JTask<T> doInForeground() {
        isBackground = false;
        return this;
    }

    public void start() {
        try {
            isCancelled = false;
            if (isRunning) throw new JTaskException("Task already running!", isBackground);
            isRunning = true;
            if (isBackground) {
                try {
                    if (onStart != null) onStart.run();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                assert taskRunnable != null;
                                final T o = taskRunnable.run(JTask.this);
                                getForegroundHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (taskResult != null) taskResult.onResult(o);
                                        if (onEnd != null) onEnd.run();
                                        if (next != null) next.run(o).start();
                                    }
                                });
                                isRunning = false;
                            } catch (CancellationException e) {
                                isRunning = false;
                                if (onCancel != null) {
                                    getForegroundHandler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            onCancel.run();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                isRunning = false;
                                if (onEnd != null)
                                    getForegroundHandler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            onEnd.run();
                                        }
                                    });
                                if (taskError != null) JTask.this.onError(e);
                                else throw new JTaskException(e, isBackground);
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    if (taskError != null) onError(e);
                    else throw new JTaskException(e, isBackground);
                }
            } else {
                try {
                    getForegroundHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (onStart != null) onStart.run();
                                T o = taskRunnable.run(JTask.this);
                                if (taskResult != null) {
                                    taskResult.onResult(o);
                                }
                                isRunning = false;
                                if (onEnd != null) onEnd.run();
                                if (next != null) next.run(o).start();
                            } catch (CancellationException e) {
                                isRunning = false;
                                if (onCancel != null) {
                                    onCancel.run();
                                }
                            } catch (Exception e) {
                                isRunning = false;
                                if (onEnd != null) onEnd.run();
                                if (taskError != null) JTask.this.onError(e);
                                else throw new JTaskException(e, isBackground);
                            }
                        }
                    });
                } catch (Exception e) {
                    if (taskError != null) onError(e);
                    else throw new JTaskException(e, isBackground);
                }
            }
        } catch (Exception e) {
            if (taskError != null) onError(e);
            else throw new JTaskException(e, isBackground);
        }
    }

    public void publishProgress(final Object... progress) {
        if (onProgress != null)
            getForegroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    onProgress.progress(progress);
                }
            });
    }

    public void sleep(long mills) throws InterruptedException {
        Thread.sleep(mills);
    }

    public JTask<T> then(NextJTask<T> nextTask) {
        next = nextTask;
        return this;
    }

    public void cancel() {
        isCancelled = true;
    }

    public boolean isActive() {
        return !isCancelled;
    }

    public void ensureActive() {
        if (!isActive()) throw new CancellationException();
    }

    private void onError(final Exception e) {
        getForegroundHandler().post(new Runnable() {
            @Override
            public void run() {
                taskError.onError(e);
            }
        });
    }

    public static class Foreground {
        public static void start(Runnable runnable) {
            getForegroundHandler().post(runnable);
        }
    }

    public static class Background {
        public static void start(Runnable runnable) {
            new Thread(runnable).start();
        }
    }
}
