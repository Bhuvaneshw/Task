# Task
Android Library for background and foreground tasks. Implemented with Thread.<br>
Do not start large number of Task concurrently.(Use [CoroutineTask](../coroutinetask) instead if possible)<br>
Starting large number of Threads may leads to out of memory.

 [See Examples](../../README.md)

# Task (Java) Documentation

## Constructor
```
Task(TaskRunnable<T> taskRunnable)
```

## Static Members
### with
Constructs new Task object
```
Task<?> with(TaskRunnable<?> taskRunnable)
```

## Callbacks

### onStart
Called before the execution of task
```
Task<T> onStart(TaskCallback onStart)
```

### onEnd
Called after the execution of task regardless the completion of the task (Whether the task is executed without error or not)
```
Task<T> onEnd(TaskCallback onEnd)
```

### onResult
Called when the task is successfully completed without error

```
Task<T> onResult(TaskResult<T> taskResult)
```

### onError
Called when the task is failed
```
Task<T> onError(TaskError taskError)
```

### onCancel
Called when the execution of task is cancelled
```
Task<T> onCancel(TaskCallback onCancel) 
```

### onProgress
Called to update progress (in UI) by publishProgress function in UI Thread
```
Task<T> onProgress(OnProgress onProgress)
```

### doInBackground
Runs the task in background thread
```
Task<T> doInBackground()
```

### doInForeground
Runs the task in foreground thread (Main thread or UI thread)
```
Task<T> doInForeground()
```

### then
Chains next Task
```
Task<T> then(NextTask<T> nextTask)
```

## Methods

### start
Starts the execution of the task
```
void start()
```

### publishProgress
Publish the progress in UI Thread
```
Task<T> publishProgress(final Object... progress)
```

### sleep
Sleeps thread for given milliseconds
```
 Task<T> sleep(long mills) throws InterruptedException
```

### cancel
Sets the cancellation flag
```
Task<T> cancel()
```

### isActive
Returns state of task
```
boolean isActive()
```

### ensureActive
Ensure task is alive (ie, task is not cancelled)
```
void ensureActive()
```

### Background Class
#### start
Runs a simple task in new thread
```
static void start(java.lang.Runnable runnable)
```

### Foreground Class
#### start
Runs a simple task in UI thread
```
static void start(java.lang.Runnable runnable)
```