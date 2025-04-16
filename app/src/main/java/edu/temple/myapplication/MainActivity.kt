package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.BufferedReader
import java.io.IOException


class MainActivity : AppCompatActivity() {

    // -------- CREATING FILE FOR STORAGE --------- //
    // create a file to store the number
    private val internalFilename = "my_file"
    private lateinit var file: File
    // -------- CREATING FILE FOR STORAGE --------- //

    // OUTSIDE OF ONCREATE -------
    lateinit var timerTextView : TextView
    // handler
    val timerHandler = Handler(Looper.getMainLooper()) {
        it.what.toString().also { timerTextView.text = it }
        true
    }

    // timer binder
    var timerBinder : TimerService.TimerBinder? = null
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            timerBinder = p1 as TimerService.TimerBinder
            timerBinder!!.setHandler(timerHandler)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            timerBinder = null
        }
    }

    // ON CREATE ------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerTextView = findViewById(R.id.textView)

        // -------- CREATING FILE FOR STORAGE --------- //
        // create a file to store the number
        file = File(filesDir, internalFilename)
        // -------- CREATING FILE FOR STORAGE --------- //

        // -------- LOAD SAVED TIMER --------- //
        if (file.exists()) {
            try {
                val br = BufferedReader(FileReader(file))
                val text = StringBuilder()
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    text.append(line)
                    text.append('\n')
                }
                br.close()
                timerTextView.setText(text.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        // -------- LOAD SAVED TIMER --------- //

        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )

        findViewById<Button>(R.id.startButton).setOnClickListener {
            // if it's connected and timer is paused
            timerBinder?.run {
                if (!isRunning) start(10)
                else if (isRunning) pause()
            }
        }
        
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            timerBinder?.stop()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            // start button clicked
            R.id.action_start -> {
                timerBinder?.run {
                    if (!isRunning) start(10)
                    else if (isRunning) pause()
                }
            }
            // stop button clicked
            R.id.action_stop -> {
                timerBinder?.stop()
            }

            // if not activity's menu item clicked,
            else -> return false
        }

        return true
    }

    // -------- SAVE FUNCTION --------- //
    fun save() {
        try {
            val outputStream = FileOutputStream(file)
            outputStream.write(timerTextView.text.toString().toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // -------- SAVE FUNCTION --------- //

    // -------- SAVE WHEN EXITING APP --------- //
    override fun onStop() {
        super.onStop()

        // delete file if timer is not paused
        timerBinder?.run {
            if (!paused) file.delete()
            else save()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // delete file if timer is not paused
        timerBinder?.run {
            if (!paused) file.delete()
            else save()
        }
    }
    // -------- SAVE WHEN EXITING APP --------- //
}