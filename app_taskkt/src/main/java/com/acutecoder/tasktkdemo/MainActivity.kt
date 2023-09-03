package com.acutecoder.tasktkdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.acutecoder.task.Task

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textview)

        Task {//Outer Task
            Task.Foreground.start {
                Toast.makeText(
                    this@MainActivity,
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
                Task {//Inner Task
                    sleep(800)
                    publishProgress("Result of outer task is $it")
                    sleep(800)
                    "CT"
                }.onStart {
                    textView.append("\nStarting chained task")
                }.onEnd {
                    textView.append("\nChained task Finished")
                    startCancellableTask(textView)
                }.onResult { result ->
                    textView.append("\nChained task Result $result")
                }.onError { error ->
                    textView.append("\nChained task Error " + error.message)
                }.onProgress {
                    textView.append("\nChained task Progress ${it[0]}")
                }
            }.start() //this start method belongs to outer task and not inner task
        //NOTE: You should not call start method of chained task. It will be called by outer task when it is completed.
    }

    private fun startCancellableTask(textView: TextView) {
        //Cancelling of task
        val task = Task {
            var i = 10
            while (i-- > 0) {
                ensureActive() // Checks if the task is cancelled. If yes then moves to onCancel
                publishProgress(i, "Running...")
                sleep(500)
            }
            "Hello"
        }.onStart {
            textView.append("\n\n\nStarting Cancellable Task: Click here to cancel")
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
        task.start()

        textView.setOnClickListener { task.cancel() }
    }
}