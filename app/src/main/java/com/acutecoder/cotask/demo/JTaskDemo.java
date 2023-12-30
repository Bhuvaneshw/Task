package com.acutecoder.cotask.demo;

import android.util.Log;

import com.acutecoder.jtask.JTask;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Bhuvaneshwaran
 * <p>
 * 9:37 PM, 12/30/2023
 *
 * @author AcuteCoder
 */

public class JTaskDemo {
    public static void run() {
        simpleTask();
        callbackTask();
        chainTask();
        cancelableTask();
    }

    private static void simpleTask() {
        JTask.with(task -> {
            task.sleep(1000);
            return "hello";
        }).start();
    }

    private static void callbackTask() {
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
    }

    private static void chainTask() {
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
    }

    private static void cancelableTask() {
        JTask<?> task = JTask.with(t -> {
            int i = 0;
            while (i < 10) {
                t.ensureActive();                         // Mandatory if you cancel the task!
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
    }

    private static void log(String msg) {
        Log.d("Task", msg);
    }
}
