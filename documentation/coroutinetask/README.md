# Task
Android Library for background and foreground tasks.

 [See Examples](../../README.md)

# CoroutineTask Documentation

## Constructor
```
CoroutineTask(
    dispatcher: CoroutineDispatcher? = null,
    private val scope: CoroutineScope = dispatcher?.let { scopeWithDispatcher(it) } ?: DEFAULT_SCOPE,
    private val runnable: suspend CoroutineTask<T>.() -> T
): AbstractTask<T> 
```

## Companion members
```
CoroutineTask.DEFAULT_SCOPE
```

## Callbacks

### onStart
Called before the execution of task
```
onStart(onStart: (() -> Unit)?): CoroutineTask<T>
```

### onEnd
Called after the execution of task regardless the completion of the task (Whether the task is executed without error or not)
```
onEnd(onEnd: (() -> Unit)?): CoroutineTask<T>
```

### onResult
Called when the task is successfully completed without error

```
onResult(taskResult: ((result: T) -> Unit)?): CoroutineTask<T>
```

### onError
Called when the task is failed
```
onError(taskError: ((error: Exception) -> Unit)?): CoroutineTask<T>
```

### onCancel
Called when the execution of task is cancelled
```
onCancel(onCancel: (() -> Unit)?): CoroutineTask<T> 
```

### onProgress
Called to update progress (in UI) by publishProgress function in UI Thread
```
onProgress(onProgress: ((Array<out Any>) -> Unit)?): CoroutineTask<T>
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
suspend sleep(mills: Long)
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

### async
Same as Coroutine.async
```
async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T>
```

### asyncSafely
Same as Coroutine async but Handles exception to task callback
```
asyncSafely(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T?>
```

### withContextSafely
Same as withContext of Coroutine builder but Handles exception to task callback
```
suspend <T> withContextSafely(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T?
```