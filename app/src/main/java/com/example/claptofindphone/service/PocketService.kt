package com.example.claptofindphone.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.claptofindphone.activity.PocketPopUpActivity
import com.example.claptofindphone.database.ToneDatabase
import com.example.claptofindphone.utils.ByteArrayMediaDataSource
import com.example.claptofindphone.utils.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PocketService : Service() {

    private var _notification: Notification? = null
    private lateinit var mPlayer: MediaPlayer
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private var listener: SensorEventListener? = null
    private lateinit var dataStoreRepo: DataStoreRepository
    private val job = SupervisorJob()


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val database = ToneDatabase.getDatabase(application)
        dataStoreRepo = DataStoreRepository(this)

        CoroutineScope(Dispatchers.IO + job).launch {
            val tone = database.toneDao().getSelectedTone()
            mPlayer = MediaPlayer()
            mPlayer.setDataSource(ByteArrayMediaDataSource(tone.bytes))
            mPlayer.prepareAsync()
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0)


        if (Build.VERSION.SDK_INT >= 26) {
            val channelName = "Background Service"
            val channel = NotificationChannel(
                "example.permanence",
                channelName,
                NotificationManager.IMPORTANCE_NONE
            )
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(channel)
            val notificationBuilder = NotificationCompat.Builder(this, "example.permanence")
            _notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        }
        if (Build.VERSION.SDK_INT >= 26) {
            startForeground(10101, _notification)
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager


        proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (proximitySensor == null) {
            Toast.makeText(this, "No proximity sensor found in device.", Toast.LENGTH_SHORT).show()
        } else {
            var isFirstTime = true
            listener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

                }

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null) {
                        if (isFirstTime) {
                            isFirstTime = false
                            return
                        }
                        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                            if (event.values[0].toInt() == 0) {

                                if (mPlayer.isPlaying)
                                    mPlayer.pause()

                            } else {
                                if (!mPlayer.isPlaying)
                                    mPlayer.start()
                                val intent =
                                    Intent(
                                        this@PocketService,
                                        PocketPopUpActivity::class.java
                                    )
                                sensorManager?.unregisterListener(listener)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                        }
                    }

                }
            }

            sensorManager!!.registerListener(
                listener, proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )

        }

        return START_STICKY
    }

    override fun onDestroy() {
        mPlayer.stop()
        super.onDestroy()
    }

}