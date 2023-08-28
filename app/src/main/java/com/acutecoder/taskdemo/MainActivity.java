package com.acutecoder.taskdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.acutecoder.task.TaskRunnable;
import com.acutecoder.task.TaskCallback;
import com.acutecoder.task.TaskError;
import com.acutecoder.task.TaskResult;
import com.acutecoder.task.Task;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textview);
        //Java 1.7
        new Task<>(new TaskRunnable<String>() {
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
                .onStart(new TaskCallback() {
                    @Override
                    public void run() {
                        textView.setText("Starting task");
                    }
                })
                .onEnd(new TaskCallback() {
                    @Override
                    public void run() {
                        textView.append("\n" + "Task Finished");
                    }
                })
                .onResult(new TaskResult<String>() {
                    @Override
                    public void onResult(String result) {
                        textView.append("\n" + result);
                    }
                })
                .onError(new TaskError() {
                    @Override
                    public void onError(Exception e) {
                        textView.append("\n" + e.getMessage());
                    }
                })
                .start();

        //Java 1.8
        Task.with(() -> null)
                .doInBackground() //or .doInForeground() # default it will be background task
                .onStart(() -> textView.setText("Starting task"))
                .onEnd(() -> textView.append("\n" + "Task Finished"))
                .onResult(result -> textView.append("\n" + result))
                .onError(e -> textView.append("\n" + e.getMessage()))
                .start();
    }
}