# Task
Android Library for background and foreground tasks.<br>

### [For Kotlin check this](https://github.com/Bhuvaneshw/TaskKT)

## Implementation (Gradle, Java)
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
    implementation 'com.github.Bhuvaneshw:task:1.1.3'
}
```

## Latest Version
[![](https://jitpack.io/v/Bhuvaneshw/task.svg)](https://jitpack.io/#Bhuvaneshw/task)

## How to Use

### Example
```
Task.with(task -> { //or new Task<>(task -> {
            Task.Foreground.start(() -> Toast.makeText(this, "This is how you can toast with Task", Toast.LENGTH_SHORT).show());
            task.sleep(1000);
            return null;
        }).start();
```

Optional methods
 1. onStart
 2. onEnd
 3. onResult
 4. onError
 5. doInBackground
 6. doInForeground
 7. then
 
### Complete Example
```
new Task<>(task -> {//Outer Task
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
        .doInBackground() //or .doInForeground() # default it will be background task
        .onStart(() -> textView.append("\nStarting task 2"))
        .onEnd(() -> textView.append("\n" + "Task 2 Finished"))
        .onResult(result -> textView.append("\nTask 2 " + result))
        .onError(error -> textView.append("\nTask 2 " + error.getMessage()))
        .onProgress(progress -> textView.append("\nTask 2 Progress " + progress[0]))
        .then(result ->
                new Task<>(t2 -> {//Inner Task
                    t2.sleep(2000);
                    t2.publishProgress("Result of outer task is " + result);
                    t2.sleep(2000);
                    return "Task 2 chained result";
                }).onEnd(() -> textView.append("\nChained task 2 finished")
                ).onProgress(progress -> textView.append("\nChained Task 2 Progress " + progress[0])
                ).onResult(result2 -> textView.append("\n" + result2)))
        .start();//this start method belongs to outer task and not inner task
//NOTE: You should not call start method of chained task. It will be called by outer task when it is completed.
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
            .then(new NextTask<String>() {
                @Override
                public Task<?> run(String result) {
                    return new Task<>(new TaskRunnable<String>() {//Inner Task
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
