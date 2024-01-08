# Task
Task Handling library for Kotlin and Android.
This library is based on Kotlin Coroutine and Thread
<br><br>
<b>Why CoTask</b><br>
1. Pause CoTask<br>
2. Resume CoTask<br>
3. Publish progress to main thread<br>
4. Error handling and Other callbacks<br><br>

Contents:<br>
<a href="#1-setup">1. Setup</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11-gradle---kotlin-dsl">1.1 Kotlin DSL</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12-gradle---groovy-dsl">1.2 Groovy DSL</a><br>
<a href="#2-usage">2. Usage</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-cotask---coroutine-task-for-kotlin">2.1 CoTask</a> (Recommended)<br>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-threadtask---for-kotlin">2.2 ThreadTask</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-jtask---for-java">2.3 JTask</a><br>
<a href="#3-license">3. License</a>
<hr>
<br>

## 1. Setup

### 1.1 Gradle - Kotlin DSL
Step 1: Project level build.gradle / settings.gradle
<pre>

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven {
            <b>url = uri("https://jitpack.io")</b>
        }
    }
}
</pre>

Step 2: Module level build.gradle<br>
<pre>
dependencies {
    <b>implementation("com.github.Bhuvaneshw.task:<i>$module:$version</i>")</b>
}
</pre>
Replace <b>$module</b> with <i><b>cotask, threadtask</b> or <b>jtask</b></i><br>
Replace <b>$version</b> with latest version<br>
Latest Version: [![](https://jitpack.io/v/Bhuvaneshw/task.svg)](https://jitpack.io/#Bhuvaneshw/task)<br><br>
<b>Example:</b>
<pre>
dependencies {
    <b>implementation("com.github.Bhuvaneshw.task:<i>cotask:2.0.0</i>")</b>
    <b>implementation("com.github.Bhuvaneshw.task:<i>threadtask:2.0.0</i>")</b>
    <b>implementation("com.github.Bhuvaneshw.task:<i>jtask:2.0.0</i>")</b>
}
</pre>

### 1.2 Gradle - Groovy DSL
Step 1: Project level build.gradle / settings.gradle
<pre>
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        <b>maven { url 'https://jitpack.io' }</b>
    }
}
</pre>

Step 2: Module level build.gradle<br>
<pre>
dependencies {
    <b>implementation 'com.github.Bhuvaneshw.task:<i>$module:$version</i>'</b>
}
</pre>
<hr>
<br>

## 2. Usage
### 2.1 CoTask - Coroutine Task, for Kotlin<br>

Simple CoTask
<pre>
CoTask {              // Default dispatcher will be Dispatchers.Default
    delay(1000)       // Your expensive task
}
</pre>
<br>

Using Coroutine Functions
<pre>
CoTask {              // Task&lt;Unit,Nothing&gt;:CoroutineScope
    delay(1000)
    launch {          // from coroutine library
        delay(1000)
    }
    val job = async { // from coroutine library
        delay(1000)
    }
    job.await()
}
</pre>
<br>

Callbacks
<pre>
CoTask {    // this: Task&lt;String, Nothing&gt; String => return type
    delay(1000)
    "My valuable result"
}.onPause {
    appendStatus("CoTask1 Paused")
}.onResume {
    appendStatus("CoTask1 Resumed")
}.onEnd {
    appendStatus("CoTask1 completed")
}.onCancelled {
    appendStatus("CoTask1 cancelled")
}.onResult { result: String ->
    appendStatus("CoTask1 result $result")
}
</pre>
<br>

Error Handling
<pre>
CoTask {
    delay(4000)
    5 / 0                     // Divide by zero
}.catch {
    appendStatus("CoTask error $it")
}
<b>// Or</b>
CoTask {
    delay(4000)
    5 / 0                     // Divide by zero
}.logError("CoTask")
</pre>
<br>

Chaining Tasks
<pre>
CoTask {                   // this: Task&lt;String, Nothing&gt; String => return type
    delay(1000)
    "500"
}.then { it: String ->     // this: Task<Int, Nothing>, it:String => the previous return value
    delay(2000)
    it.toInt()
}.then { it: Int ->
    it / 5f
}.onResult { result: Float ->
    appendStatus("CoTask2 result $result")
}
</pre>
<br>

Progressed CoTask
<pre>
ProgressedCoTask {    // this: Task&lt;String, Int&gt; String => return type, Int => Progress Type
    delay(1000)
    publishProgress(50)
    delay(1000)
    publishProgress(99)
    "My valuable result"
}.onProgress { progress: Int ->
    appendStatus("CoTask3 progress $progress")
}.onResult { result: String ->
    appendStatus("CoTask3 result $result")
}
</pre>
<br>

Cancelling Task
<pre>
val task = ProgressedCoTask {
    var i = 10
    while (i-- > 0) {
        ensureActive()            // enabling that the task can be paused/cancelled here
        publishProgress(10 - i)
    }
}.onProgress {
    appendStatus("CoTask4 progress $it")
}.onCancelled {
    appendStatus("CoTask4 cancelled")
}
// Cancelling the task after 1.5 seconds
CoTask {
    delay(1500)
    task.cancel()
}
</pre>
<br>

Pausing and Resuming
<pre>
val task = ProgressedCoTask {
    var i = 10
    while (i-- > 0) {
        ensureActive()            // enabling that the task can be paused/cancelled here
        publishProgress(10 - i)
    }
}.onProgress {
    appendStatus("CoTask5 progress $it")
}.onPause {
    appendStatus("CoTask5 paused")
}.onResume {
    appendStatus("CoTask5 resumed")
}
// Pausing and Resuming the task after 1.5 seconds of break
CoTask {
    delay(1500)
    task.pause()
    delay(1500)
    task.resume()
}
</pre>
<br>

Startable Tasks
<pre>
StartableCoTask {
    delay(1000)
}.onStart {
}.start()
StartableProgressedCoTask {
    publishProgress(10)
    delay(1000)
    publishProgress(100)
}.start()

// If you return any data, then
StartableCoTask {
    delay(1111)
    "My value"
}.start { result: String ->        // called before on result callback
    appendStatus("Result $result")
}
</pre>
<br>

Using with scopes, Extension functions
<pre>
GlobalScope.coTask { }
GlobalScope.progressedCoTask { publishProgress(0) }
GlobalScope.startableCoTask { }
GlobalScope.startableProgressedCoTask { publishProgress(0) }
</pre>
<hr>
<br>

### 2.2 ThreadTask - for Kotlin
<br>Note: Pausing and Resuming is not available in ThreadTask

Simple Thread Task
<pre>
ThreadTask {
    delay(1000)       // Your expensive task
}
</pre>
<br>

Callbacks
<pre>
ThreadTask {    // this: Task&lt;String, Nothing&gt; String => return type
    delay(1000)
    "My valuable result"
}.onEnd {
    appendStatus("ThreadTask1 completed")
}.onCancelled {
    appendStatus("ThreadTask1 cancelled")
}.onResult { result: String ->
    appendStatus("ThreadTask1 result $result")
}
</pre>
<br>

Error Handling
<pre>
ThreadTask {
    delay(4000)
    5 / 0                     // Divide by zero
}.catch {
    appendStatus("ThreadTask error $it")
}
// Or
ThreadTask {
    delay(4000)
    5 / 0                    // Divide by zero
}.logError("ThreadTask")
</pre>
<br>

Chaining Tasks
<pre>
ThreadTask {               // this: Task&lt;String, Nothing&gt; String => return type
    delay(1000)
    "500"
}.then { it: String ->     // this: Task<Int, Nothing>, it:String => the previous return value
    delay(2000)
    it.toInt()
}.then { it: Int ->
    it / 5f
}.onResult { result: Float ->
    appendStatus("ThreadTask2 result $result")
}
</pre>
<br>

ProgressedThreadTask
<pre>
ProgressedThreadTask {    // this: Task&lt;String, Int&gt; String => return type, Int => Progress Type
    delay(1000)
    publishProgress(50)
    delay(1000)
    publishProgress(99)
    "My valuable result"
}.onProgress { progress: Int ->
    appendStatus("ThreadTask3 progress $progress")
}.onResult { result: String ->
    appendStatus("ThreadTask3 result $result")
}
</pre>
<br>

Cancelling Task
<pre>
val task = ProgressedThreadTask {
    var i = 1
    while (i <= 100) {
        publishProgress(i)
        ensureActive()            // Mandatory for ThreadTask to check for cancellation and calling onCancelled callback
        delay(1000)
        i += 10
    }
}.onProgress {
    appendStatus("ThreadTask4 progress $it")
}.onCancelled {
    appendStatus("ThreadTask4 cancelled")
}

// Cancelling the task after 1.5 seconds
ThreadTask {
    delay(1500)
    task.cancel()
}
</pre>
<br>

Startable Tasks
<pre>
StartableThreadTask {
    delay(1000)
}.onStart{
}.start()
StartableProgressedThreadTask {
    publishProgress(10)
    delay(1000)
    publishProgress(100)
}.start()

// If you return any data, then
StartableThreadTask {
    delay(1111)
    "My value"
}.start { result: String ->        // called before on result callback
    appendStatus("Result $result")
}
</pre>
<hr>
<br>

### 2.3 JTask - for Java

Simple JTask
<pre>
JTask.with(task -> {
    task.sleep(1000);
    return "hello";
}).start();
</pre>
<br>

Callbacks and Error handling
<pre>
JTask.with(task -> {
    int i = 0;
    while (i < 10) {
        task.ensureActive();                         // Mandatory if you cancel the task!
         task.publishProgress((i + 1) * 10);
        i++;
    }
    return "hello";
}).onStart(() -> {
    log("OnStart");
}).onEnd(() -> {
    log("OnEnd");
}).onCancel(() -> {
    log("OnCancel");
}).onError((error) -> {
    log("OnError : " + error.getLocalizedMessage());
}).onProgress((progress) -> {
    log("Progress: " + ((int) progress[0]));
}).onResult((result) -> {
    log("Result: " + result);
}).start();
</pre>
<br>

Chaining Tasks
<pre>
new JTask<String>(task -> {
    task.sleep(1000);
    return "123";
}).then(result ->
    new JTask<Integer>(task -> {
        task.sleep(1000);
        return Integer.parseInt(result);
    }).onStart(() -> {
        log("OnStart");
    }).onResult(intResult -> {
        log("Result: " + intResult);
    })
).start();
</pre>

Cancelling Task
<pre>
JTask<?> task = JTask.with(t -> {
    int i = 0;
    while (i < 10) {
        t.ensureActive();       // Mandatory if you cancel the task!
        t.publishProgress((i + 1) * 10);
        i++;
    }
    return "hello";
}).onCancel(() -> {
    log("OnCancel");
});
task.start();

new Timer().schedule(new TimerTask() {
    @Override
    public void run() {
        task.cancel();
    }
}, 1000);
</pre>
<hr>
<br>

## 3. License
```
    Task - Task Handling Library
    Copyright (C) 2024  Bhuvaneshwaran

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
