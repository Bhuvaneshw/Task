# Task
Task Handling library for Kotlin and Android
This library is based on Kotlin Coroutine and Thread

Contents:<br>
<a href="#setup">1. Setup</a><br>
<a href="#setup">2. Usage</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#setup">2.1 CoTask</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#setup">2.2 ThreadTask</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#setup">2.3 JTask</a><br>

## 1.Setup
### 1.1 Gradle - Groovy
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
Replace <b>$module</b> with <i><b>cotask, threadtask</b> or <b>jtask</b></i><br>
Replace <b>$version</b> with latest version<br>
Latest Version: [![](https://jitpack.io/v/Bhuvaneshw/task.svg)](https://jitpack.io/#Bhuvaneshw/task)<br>
<b>Example:</b>
<pre>
dependencies {
    <b>implementation 'com.github.Bhuvaneshw.task:<i>cotask:2.0.0</i>'</b>
}
</pre>

## 2.Usage
Usage of the library
### 2.1 CoTask - Coroutine Task<br>
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
