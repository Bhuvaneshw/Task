package com.acutecoder.taskdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.acutecoder.task.Task;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textview);
        Task.with(() -> null)
                .doInBackground() //or .doInForeground() # default it will be background task
                .onStart(() -> textView.setText("Starting task"))
                .onEnd(() -> textView.append("\n" + "Task Finished"))
                .onResult(result -> textView.append("\n" + result))
                .onError(e -> textView.append("\n" + e.getMessage()))
                .start();
    }
}