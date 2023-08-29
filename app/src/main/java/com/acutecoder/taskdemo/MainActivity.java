package com.acutecoder.taskdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.acutecoder.task.NextTask;
import com.acutecoder.task.OnProgress;
import com.acutecoder.task.Task;
import com.acutecoder.task.TaskCallback;
import com.acutecoder.task.TaskError;
import com.acutecoder.task.TaskResult;
import com.acutecoder.task.TaskRunnable;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textview);
        //Java 1.7
        new Task<>(new TaskRunnable<String>() { //Outer Task
            @Override
            public String run(Task<String> t) throws Exception {
                Task.Foreground.run(new Runnable() {
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

        //Java 1.8
        Task.with(task -> {//Outer Task
                    Task.Foreground.run(() -> Toast.makeText(this, "This is how you can toast with Task", Toast.LENGTH_SHORT).show());

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
    }
}