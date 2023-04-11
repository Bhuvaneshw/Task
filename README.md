# Task
Android Library for background and foreground tasks

## How to Use

### Example
```
Task.with(() -> {
            Task.Foreground.run(()-> Toast.makeText(this, "Toast in background task", Toast.LENGTH_SHORT).show()); // Toast in UI Thread
            Thread.sleep(1000);
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
 
### Example
```
Task.with(() -> {
            Task.Foreground.run(()-> Toast.makeText(this, "Toast in background task", Toast.LENGTH_SHORT).show()); // Toast in UI Thread
            Thread.sleep(1000);
            return "Hello";
        })
          .doInBackground() //or .doInForeground() # default it will be background task
          .onStart(() -> textView.setText("Starting task"))
          .onEnd(() -> textView.append("\n" + "Task Finished"))
          .onResult(result -> textView.append("\n" + result))
          .onError(e -> textView.append("\n" + e.getMessage()))
          .start();
```

## For Java 1.7

### Example
```
new Task.Java_1_7<>(new Task.Runnable<String>() {
            @Override
            public String run() throws Exception {
                Task.Foreground.run(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Toast in background task", Toast.LENGTH_SHORT).show(); // Toast in UI Thread
                    }
                });
                Thread.sleep(1000);
                return "Hello";
            }
        })
          .doInBackground() //or .doInForeground() # default it will be background task
          .onStart(new Task.Callback() {
              @Override
              public void callback() {
                  textView.setText("Starting task");
              }
          })
          .onEnd(new Task.Callback() {
              @Override
              public void callback() {
                  textView.append("\n" + "Task Finished");
              }
          })
          .onResult(new Task.Result<String>() {
              @Override
              public void onResult(String result) {
                  textView.append("\n" + result);
              }
          })
          .onError(new Task.Error() {
              @Override
              public void onError(Exception e) {
                  textView.append("\n" + e.getMessage());
              }
          })
          .start();
```

## License
```
    AcuteCoder/task - Android Library for background and foreground tasks
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
