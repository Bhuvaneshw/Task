# Task
Android Library for background and foreground tasks. Implemented with Thread.<br>
Do not start large number of Task concurrently.(Use [CoroutineTask](../coroutinetask) instead)<br>
Starting large number of Threads may leads to out of memory.

 [See Examples](../../README.md)

# Task (Kotlin) Documentation

## Constructor
```
Task<T>(private val runnable: Task<T>.() -> T) : AbstractTask<T>
```

## Callbacks

### onStart
Called before the execution of task
```
onStart(onStart: (() -> Unit)?): Task<T>
```

### onEnd
Called after the execution of task regardless the completion of the task (Whether the task is executed without error or not)
```
onEnd(onEnd: (() -> Unit)?): Task<T>
```

### onResult
Called when the task is successfully completed without error

```
onResult(taskResult: ((result: T) -> Unit)?): Task<T>
```

### onError
Called when the task is failed
```
onError(taskError: ((error: Exception) -> Unit)?): Task<T>
```

### onCancel
Called when the execution of task is cancelled
```
onCancel(onCancel: (() -> Unit)?): Task<T> 
```

### onProgress
Called to update progress (in UI) by publishProgress function in UI Thread
```
onProgress(onProgress: ((Array<out Any>) -> Unit)?): Task<T>
```

### then
Chains next Task
```
then(nextTask: ((T) -> AbstractTask<*>)?): Task<T>
```

## Methods

### start
Starts the execution of the task
```
start(): Unit
```

### publishProgress
Publish the progress in UI Thread
```
publishProgress(vararg progress: Any): Task<T>
```

### sleep
Sleeps thread for given milliseconds
```
sleep(mills: Long)
```

### cancel
Sets the cancellation flag
```
cancel(): CoroutineTask<T>
```

### isActive
Returns state of task
```
isActive(): Boolean
```

### ensureActive
Ensure task is alive (ie, task is not cancelled)
```
ensureActive(): Unit
```

### Background object
#### start
Runs a simple task in new thread
```
start(runnable: Runnable?)
```

### Foreground object
#### start
Runs a simple task in UI thread
```
start(runnable: Runnable?)
```