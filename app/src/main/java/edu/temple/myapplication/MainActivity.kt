package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button

class MainActivity : AppCompatActivity() {

    // OUTSIDE OF ONCREATE
    var timerBinder : TimerService.TimerBinder? = null

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            timerBinder = p1 as TimerService.TimerBinder
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            timerBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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