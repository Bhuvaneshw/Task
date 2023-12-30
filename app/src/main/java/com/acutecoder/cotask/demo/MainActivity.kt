package com.acutecoder.cotask.demo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.acutecoder.cotask.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var v: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivityMainBinding.inflate(layoutInflater)
        setContentView(v.root)

        v.start.setOnClickListener {
            coTaskExample(this)
            threadTaskExample(this)
            JTaskDemo.run()
        }
        v.download.setOnClickListener {
            startActivity(Intent(this, FileDownloadingActivity::class.java))
        }
    }

    fun appendStatus(msg: String) {
        v.status.append("\n$msg")
    }
}
