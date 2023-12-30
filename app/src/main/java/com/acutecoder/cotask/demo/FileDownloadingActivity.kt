package com.acutecoder.cotask.demo

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.acutecoder.cotask.ProgressedCoTask
import com.acutecoder.cotask.base.Task
import com.acutecoder.cotask.base.TaskHandler
import com.acutecoder.cotask.demo.databinding.ActivityFileDownloadingBinding
import com.acutecoder.cotask.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

class FileDownloadingActivity : AppCompatActivity() {

    private lateinit var v: ActivityFileDownloadingBinding
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivityFileDownloadingBinding.inflate(layoutInflater)
        setContentView(v.root)

        v.download.setOnClickListener { downloadFile() }
    }

    @SuppressLint("SetTextI18n")
    private fun downloadFile() {
        val downloadTask = downloadImage()
            .onProgress { progress ->
                if (v.progressBar.isIndeterminate) v.progressBar.isIndeterminate = false
                v.progressBar.progress = progress
                v.progressText.text = "$progress%"
            }.onCancelled {
                restoreUi()
                appendStatus("Task Cancelled")
            }.onPause {
                appendStatus("Task Paused")
            }.onResume {
                appendStatus("Task Resumed")
            }.catch {
                appendStatus("Error $it")
                restoreUi()
            }.onEnd {
                appendStatus("Task Ended")
            }.then { path ->
                withMain { v.progressBar.isIndeterminate = true }
                loadBitmap(path)
            }.thenCompressBitmap()
            .thenResizeBitmap()
            .onResult { bitmap ->
                this.bitmap = bitmap
                loadImage(bitmap)
                restoreUi()
                appendStatus("Click the image to view in fullscreen")
            }

        setUi(downloadTask)
    }

    private fun loadImage(bitmap: Bitmap) {
        appendStatus("Loading Image")
        v.imageView.setImageBitmap(bitmap)
        appendStatus("Image loaded")
    }

    private fun downloadImage() = ProgressedCoTask {
        val url = v.urlBox.text.toString()
        val path = "$cacheDir/temp.png"

        // Running both createFile and connect simultaneously with the help of async
        val connect = async {
            withMain { appendStatus("Connecting") }
            connect(url)
        }

        val createFile = async {
            withMain { appendStatus("Creating File") }
            createNewFile(path)
        }

        val (inputStream, total) = connect.await()
        val outputStream = FileOutputStream(createFile.await())
        withMain { appendStatus("Connected") }

        val buffer = ByteArray(1024)
        var read: Int
        var downloaded = 0
        var previousProgress = 0
        try {
            withMain { appendStatus("Downloading") }
            publishProgress(0)
            while ((inputStream.read(buffer).also { read = it }) != -1) {
                ensureActive()
                downloaded += read
                val progress = (downloaded * 100 / total).toInt()
                if (previousProgress != progress) {
                    previousProgress = progress
                    publishProgress(progress)
                }
                outputStream.write(buffer, 0, read)
            }
        } catch (e: Exception) {
            throw e
        } finally {
            inputStream.close()
            outputStream.close()
            outputStream.flush()
        }
        withMain { appendStatus("Downloaded") }

        path
    }

    //Example of using suspend functions
    private suspend fun createNewFile(path: String): File {
        return File(path).apply {
            withContext(Dispatchers.IO) {
                if (!exists()) {
                    createNewFile()
                }
            }
        }
    }

    //Example of using suspend functions
    private suspend fun connect(url: String): Pair<InputStream, Long> {
        val connection = URL(url).openConnection()
        withContext(Dispatchers.IO) {
            connection.connect()
        }

        return connection.getInputStream() to
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) connection.contentLengthLong
                else connection.contentLength.toLong()
    }

    private suspend fun Task<Bitmap, Int>.loadBitmap(path: String): Bitmap {
        withMain { appendStatus("Loading Bitmap") }
        val bitmap = BitmapFactory.decodeFile(path)
        withMain { appendStatus("Bitmap Loaded") }
        return bitmap
    }

    private fun TaskHandler<Bitmap, Int>.thenCompressBitmap() =
        then { source ->
            withMain { appendStatus("Compressing Bitmap") }
            val out = ByteArrayOutputStream(source.byteCount)
            source.compress(Bitmap.CompressFormat.JPEG, 70, out)
            val inp = ByteArrayInputStream(out.toByteArray())
            val compressedBitmap = BitmapFactory.decodeStream(inp)
            withMain { appendStatus("Bitmap Compressed") }
            compressedBitmap
        }

    private fun TaskHandler<Bitmap, Int>.thenResizeBitmap() =
        then { bitmap ->
            resizeBitmap(bitmap, v.imageView.width)
        }

    private suspend fun Task<Bitmap, Int>.resizeBitmap(source: Bitmap, newWidth: Int): Bitmap {
        withMain { appendStatus("Resizing Bitmap") }
        val originalWidth = source.width
        val originalHeight = source.height

        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        val newHeight = (newWidth / aspectRatio).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
        withMain { appendStatus("Bitmap resized") }
        return scaledBitmap
    }

    private fun appendStatus(msg: String) {
        v.status.post { v.status.append("\n$msg") }
        v.scroller.post { v.scroller.fullScroll(View.FOCUS_DOWN) }
    }

    private fun rotateBitmap(originalBitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)

        return Bitmap.createBitmap(
            originalBitmap,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height,
            matrix,
            true
        )
    }

    @SuppressLint("SetTextI18n")
    private fun restoreUi() {
        v.download.apply {
            text = "Download"
            setOnClickListener {
                v.status.text = "Status:"
                v.imageView.setImageDrawable(null)
                downloadFile()
            }
        }
        v.pauseResume.visibility = View.GONE

        v.progressBar.isIndeterminate = false
        v.progressBar.progress = 0
        v.progressText.text = ""
    }

    @SuppressLint("SetTextI18n")
    private fun setUi(downloadTask: TaskHandler<Bitmap, Int>) {
        v.pauseResume.text = if (downloadTask.isPaused) "Resume" else "Pause"
        v.pauseResume.visibility = View.VISIBLE
        v.pauseResume.setOnClickListener {
            if (downloadTask.isPaused) downloadTask.resume()
            else downloadTask.pause()
            v.pauseResume.text = if (downloadTask.isPaused) "Resume" else "Pause"
        }

        v.progressBar.isIndeterminate = true
        v.progressText.text = "Waiting..."

        v.download.apply {
            text = "Cancel"
            setOnClickListener { downloadTask.cancel() }
        }

        var isRoot = false
        v.imageView.setOnClickListener {
            val rotate = v.imageView.width > v.imageView.height

            v.imageView.parent?.let { parent ->
                (parent as ViewGroup).removeView(v.imageView)
            }
            if (isRoot) {
                setContentView(
                    v.root,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                (v.scroller.getChildAt(0) as ViewGroup).addView(v.imageView)
                v.imageView.setBackgroundColor(Color.TRANSPARENT)
                isRoot = false
            } else {
                setContentView(
                    it,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                v.imageView.setBackgroundColor(Color.BLACK)
                isRoot = true
            }

            ProgressedCoTask {
                resizeBitmap(bitmap!!, v.imageView.width).let { resizedBitmap ->
                    if (rotate) {
                        rotateBitmap(resizedBitmap, if (isRoot) 90f else 0f)
                    } else resizedBitmap
                }
            }.onResult { result ->
                v.imageView.setImageBitmap(result)
            }.logError("CoTask")
        }
    }

}
