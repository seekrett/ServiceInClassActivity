package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

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
}