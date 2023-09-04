# Task
Android Library for background and foreground tasks.
## Documentation
1. **[Coroutine](documentation/coroutinetask)** (Recommended)
2. **[Task(Kotlin)](documentation/taskkt)**
3. **[Task(Java)](documentation/task)**

## Latest Version
[![](https://jitpack.io/v/Bhuvaneshw/task.svg)](https://jitpack.io/#Bhuvaneshw/task)
# Example (Kotlin+Coroutine) (Recommended)
## Gradle Setup
Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency
```
dependencies {
    implementation 'com.github.Bhuvaneshw.task:coroutine:1.1.5'
}
```
## Simple Task

```
CoroutineTask {                 // this:Task<Nothing?>
    // Do some task
    sleep(1000)                 //sleep belongs to task object
    null                        //return value
}).start();
```

## Task with all Callbacks and Chaining of Tasks
```
CoroutineTask {                                //Outer Task
    Task.Foreground.start {
        Toast.makeText(this@MainActivity, "This is how you can toast with Task", Toast.LENGTH_SHORT).show()
    }
    sleep(1000)
    publishProgress(25)
    sleep(1000)
    publishProgress(56)
    sleep(2000)
    publishProgress(100)
    sleep(500)
    "T"
}.doInBackground()                    //or .doInForeground() # default it will be background task
.onStart {
    textView.text = "Starting task"
}.onEnd {
    textView.append("\n" + "Task Finished")
}.onResult { result ->
    textView.append("\nResult $result")
}.onError { error ->
    textView.append("\nError " + error.message)
}.onCancel {
    textView.append("\nTask Cancelled")
}.onProgress {
    textView.append("\nProgress ${it[0]}")
}.then {                         //Chaining next task
    Task {                       //Inner Task
        sleep(2000)
        publishProgress("Result of outer task is $it")
        sleep(2000)
        "CT"
    }.onStart {
        textView.append("\nStarting chained task")
    }.onEnd {
        textView.append("\nChained task Finished")
    }.onResult { result ->
        textView.append("\nChained task Result $result")
    }.onError { error ->
        textView.append("\nChained task Error " + error.message)
    }.onProgress {
        textView.append("\nChained task Progress ${it[0]}")
    }
}.start()                       //this start method belongs to outer task and not inner task
//NOTE: You should not call start method of chained task. It will be called by outer task when it is completed.
```

## Task With async and withContext
Works only with **CoroutineTask**

```
val task = CoroutineTask {
    var i = 10
    val content = async {                                    // or asyncSafely
        // Both 'asyncSafely' and 'withContextSafely' handles exceptions to CoroutineTask's callbacks.
        withContextSafely(Dispatchers.IO) {                  // withContext can also be used but som callbacks may not work properly
            URL("https://mydomain.com/api/get").readText()
        }!!
    }
    val content2 = asyncSafely {
        withContext(Dispatchers.IO) {
            URL("https://mydomain.com/api/get2").readText()
        }!!
    }
    "${content.await()} ${content2.await()}"
}.onResult {
    textView.append("\nTask result $it")
}.onCancel {
    textView.append("\nTask Cancelled")
}.onError {
    textView.append("\nTask error $it")
}
task.start()

textView.setOnClickListener { task.cancel() }
```
## Task With Scope

```
CoroutineTask(scope = CoroutineScope(Dispatchers.Default + CoroutineName("myScope"))) {                 // this:Task<Nothing?>
    // Do some task
    sleep(1000)                 //sleep belongs to task object
    null                        //return value
}).start();
```
### Task With GlobalScope

```
CoroutineTask(scope = GlobalScope) {                 // this:Task<Nothing?>
    // Do some task
    sleep(1000)                 //sleep belongs to task object
    null                        //return value
}).start();
```
## Task With Dispatcher

```
CoroutineTask(Dispatchers.Default) {                 // this:Task<Nothing?>
    // Do some task
    sleep(1000)                 //sleep belongs to task object
    null                        //return value
}).start();
```

# Example (Kotlin)
## Gradle Setup
Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency
```
dependencies {
    implementation 'com.github.Bhuvaneshw.task:kotlin:1.1.5'
}
```
## Simple Task

```
Task {                          // this:Task<Nothing?>
    // Do some task
    sleep(1000)                 //sleep belongs to task object
    null                        //return value
}).start();
```

## Simple Task with Toast
```
Task {                          // this:Task<Nothing?>
    Task.Foreground.start {
            Toast.makeText(this@MainActivity, "This is how you can toast with Task", Toast.LENGTH_SHORT).show()
    }
    sleep(1000)                 //sleep belongs to task object
    null                        //return value
}).start();
```

## Optional Callbacks
1. onStart
2. onEnd
3. onResult
4. onError
5. onCancel
6. then

## Task with all callbacks and chaining of tasks
```
Task {                                //Outer Task
    Task.Foreground.start {
        Toast.makeText(this@MainActivity, "This is how you can toast with Task", Toast.LENGTH_SHORT).show()
    }
    sleep(1000)
    publishProgress(25)
    sleep(1000)
    publishProgress(56)
    sleep(2000)
    publishProgress(100)
    sleep(500)
    "T"
}.doInBackground()                    //or .doInForeground() # default it will be background task
.onStart {
    textView.text = "Starting task"
}.onEnd {
    textView.append("\n" + "Task Finished")
}.onResult { result ->
    textView.append("\nResult $result")
}.onError { error ->
    textView.append("\nError " + error.message)
}.onCancel {
    textView.append("\nTask Cancelled")
}.onProgress {
    textView.append("\nProgress ${it[0]}")
}.then {                         //Chaining tasks
    Task {                       //Inner Task
        sleep(2000)
        publishProgress("Result of outer task is $it")
        sleep(2000)
        "CT"
    }.onStart {
        textView.append("\nStarting chained task")
    }.onEnd {
        textView.append("\nChained task Finished")
    }.onResult { result ->
        textView.append("\nChained task Result $result")
    }.onError { error ->
        textView.append("\nChained task Error " + error.message)
    }.onProgress {
        textView.append("\nChained task Progress ${it[0]}")
    }
}.start()                       //this start method belongs to outer task and not inner task
//NOTE: You should not call start method of chained task. It will be called by outer task when it is completed.
```

### Cancelling task
```
val task = Task {
    var i = 10
    while (i-- > 0) {
        ensureActive()               // Checks if the task is cancelled. If yes then moves to onCancel
        publishProgress(i, "Running...")
        sleep(500)
    }
    "Hello"
}.onStart {
    textView.append("\n\n\nStarting Cancellable Task: Click here to cancel")
}.onEnd {
    textView.append("\nTask Completed")
}.onProgress {
    textView.append("\nTask Progress ${(10 - it[0] as Int) * 10}% ${it[1]}")
}.onResult {
    textView.append("\nTask result $it")
}.onCancel {
    textView.append("\nTask Cancelled")
}
task.start()

textView.setOnClickListener { task.cancel() }
```

# Example (Coroutine + Task-Kotlin)

## Gradle Setup
Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency
```
dependencies {
    implementation 'com.github.Bhuvaneshw.task:java:1.1.5'
    implementation 'com.github.Bhuvaneshw.task:coroutine:1.1.5'
}
```

## Sample
```
Task {                                          //Outer Task (Task)
    Task.Foreground.start {
        Toast.makeText(
            this@CombinationDemoActivity,
            "This is how you can toast with Task",
            Toast.LENGTH_SHORT
        ).show()
    }
    sleep(500)
    publishProgress(25)
    sleep(500)
    publishProgress(56)
    sleep(800)
    publishProgress(100)
    sleep(500)
    "T"
}.doInBackground() //or .doInForeground() # default it will be background task
.onStart {
    textView.text = "Starting task"
}.onEnd {
    textView.append("\n" + "Task Finished")
}.onResult { result ->
    textView.append("\nResult $result")
}.onError { error ->
    textView.append("\nError " + error.message)
}.onProgress {
    textView.append("\nProgress ${it[0]}")
}.then {
    CoroutineTask {                         //Inner Task (CoroutineTask)
        var i = 10
        while (i-- > 0) {
            publishProgress(i, "Running...")
            sleep(500)
        }
        "Coroutine Task Result"
    }.onStart {
        textView.append("\n\n\nStarting Coroutine Task:\n")
    }.onEnd {
        textView.append("\nTask Completed")
    }.onProgress {
        textView.append(
            "\nTask Progress ${(10 - it[0] as Int) * 10}% ${it[1]}"
        )
    }.onResult {
        textView.append("\nTask result $it")
    }.onCancel {
        textView.append("\nTask Cancelled")
    }
}.start()                                 //this start method belongs to outer task and not inner task
//NOTE: You should not call start method of chained task. It will be called by outer task when it is completed.
```

# Example (Java)

## Gradle Setup
Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency
```
dependencies {
    implementation 'com.github.Bhuvaneshw.task:java:1.1.5'
}
```

## Simple Task

```
Task.with(task -> {             //or new Task<>(task -> {
    // Do some task
    task.sleep(1000);
    return null;
}).start();
```

## Simple Task with Toast
```
Task.with(task -> {             //or new Task<>(task -> {
    Task.Foreground.start(() -> Toast.makeText(this, "This is how you can toast with Task", Toast.LENGTH_SHORT).show());
    task.sleep(1000);
    return null;
}).start();
```

## Optional Callbacks
1. onStart
2. onEnd
3. onResult
4. onError
5. onCancel
6. then

## Simple Task with all callbacks and chaining of tasks
```
new Task<>(task -> {             //Outer Task
    Task.Foreground.start(() -> Toast.makeText(this, "This is how you can toast with Task", Toast.LENGTH_SHORT).show());

    task.sleep(1000);
    task.publishProgress(25);
    task.sleep(1000) //Function chaining is also possible
            .publishProgress(56)
            .sleep(2000)
            .publishProgress(100)
            .sleep(500);
    return "T2";
})
.doInBackground()               //or .doInForeground() # default it will be background task
.onStart(() -> textView.append("\nStarting task 2"))
.onEnd(() -> textView.append("\n" + "Task 2 Finished"))
.onResult(result -> textView.append("\nTask 2 " + result))
.onError(error -> textView.append("\nTask 2 " + error.getMessage()))
.onCancel(() -> textView.append("\nTask 2 Cancelled"))
.onProgress(progress -> textView.append("\nTask 2 Progress " + progress[0]))
.then(result ->                //Chaining task
    new Task<>(t2 -> {         //Inner Task
        t2.sleep(2000);
        t2.publishProgress("Result of outer task is " + result);
        t2.sleep(2000);
        return "Task 2 chained result";
    }).onEnd(() -> textView.append("\nChained task 2 finished")
    ).onProgress(progress -> textView.append("\nChained Task 2 Progress " + progress[0])
    ).onResult(result2 -> textView.append("\n" + result2))
).start();                    //this start method belongs to outer task and not inner task
//NOTE: You should not call start method of chained task. It will be called by outer task when it is completed.
```

### Cancelling task
```
Task<?> task = Task.with(t -> {
    int i = 10;
    while (i-- > 0) {
        t.ensureActive(); // Checks if the task is cancelled. If yes then moves to onCancel
        t.publishProgress(i, "Running...");
        t.sleep(500);
    }
    return "Hello";
})
.onStart(() -> textView.append("\n\n\nStarting Cancellable Task: Click here to cancel"))
.onEnd(() -> textView.append("\nTask Completed"))
.onProgress(p -> textView.append("\nTask Progress " + (10 - (int) p[0]) * 10 + "% " + p[1]))
.onResult(r -> textView.append("\nTask result " + r))
.onCancel(() -> textView.append("\nTask Cancelled"));
task.start();

textView.setOnClickListener(v -> task.cancel());
```

## For Java 1.7
Copy [these files](https://github.com/Bhuvaneshw/Task/tree/main/task/src/main/java/com/acutecoder/task) to your project
### Example
```
new Task<>(new TaskRunnable<String>() { //Outer Task
    @Override
    public String run(Task<String> t) throws Exception {
        Task.Foreground.start(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "This is how you can toast with Task", Toast.LENGTH_SHORT).show(); // Toast in UI Thread
            }
        });
        Thread.sleep(1000);
        return "T1";
    }
})
.doInBackground() //or .doInForeground() # default it will be background task
.onStart(new TaskCallback() {
    @Override
    public void run() {
        textView.setText("Starting task1 ");
    }
})
.onEnd(new TaskCallback() {
    @Override
    public void run() {
        textView.append("\n" + "Task 1 Finished");
    }
})
.onResult(new TaskResult<String>() {
    @Override
    public void onResult(String result) {
        textView.append("\nTask 1 result " + result);
    }
})
.onError(new TaskError() {
    @Override
    public void onError(Exception e) {
        textView.append("\nTask 1 error " + e.getMessage());
    }
})
.onProgress(new OnProgress() {
    @Override
    public void progress(Object... progress) {
        textView.append("\nTask 1 Progress " + progress[0]);
    }
})
.then(new NextTask<String>() {                            //Chaining task
    @Override
    public Task<?> run(String result) {
        return new Task<>(new TaskRunnable<String>() {   //Inner Task
            @Override
            public String run(Task<String> t2) throws Exception {
                t2.sleep(2000);
                t2.publishProgress("Result of outer task is " + result);
                t2.sleep(2000);
                return "Task 1 chained result";
            }
        }).onEnd(new TaskCallback() {
            @Override
            public void run() {
                textView.append("\nChained task 1 finished");
            }
        }).onProgress(new OnProgress() {
            @Override
            public void progress(Object... progress) {
                textView.append("\nChained Task 1 Progress " + progress[0]);
            }
        }).onResult(new TaskResult<String>() {
            @Override
            public void onResult(String result) {
                textView.append("\n" + result);
            }
        });
    }
})
.start();//this start method belongs to outer task and not inner task
//NOTE: You should not call start method of chained task. It will be called by outer task when it is completed.
```

## License
```
    AcuteCoder/Task - Android Library for background and foreground tasks
    Copyright (C) 2023  Bhuvaneshwaran

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
```
