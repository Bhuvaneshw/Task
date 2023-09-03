package com.acutecoder.tasktkdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.acutecoder.task.CoroutineTask
import kotlinx.coroutines.Dispatchers
import java.net.URL

class CoroutineDemoActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.textview)

        val task = CoroutineTask {
            var i = 10
            val content = async { // or asyncSafely
                withContextSafely(Dispatchers.IO) {
                    URL("https://mydomain.com/api/get").readText()
                }!!
            }
            while (i-- > 0) {
                ensureActive() // Checks if the task is cancelled. If yes then moves to onCancel
                publishProgress(i, "Running...")
                sleep(500)
            }
            content.await()
        }.onStart {
            textView.text =
                "Coroutine Task\n\n\nStarting Cancellable Coroutine Task:\nClick here to cancel\n"
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
        }.onError {
            textView.append("\nTask error $it")
        }
        task.start()

        textView.setOnClickListener { task.cancel() }
        textView.setOnLongClickListener {
            task.start()
            return@setOnLongClickListener true
        }
    }
}