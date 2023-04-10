package com.acutecoder.taskdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.acutecoder.task.Task;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textview);
        new Task<>(() -> {
            Task.Foreground.run(() -> Toast.makeText(this, "Toast in background task", Toast.LENGTH_SHORT).show()); // Toast in UI Thread
            Thread.sleep(1000);
            return "Hello";
        })
                .inBackground() //or .inForeground() # default it will be background task
                .onStart(() -> textView.setText("Starting task"))
                .onEnd(() -> textView.append("\n" + "Task Finished"))
                .onResult(result -> textView.append("\n" + result))
                .onError(e -> textView.append("\n" + e.getMessage()))
                .start();
    }
}