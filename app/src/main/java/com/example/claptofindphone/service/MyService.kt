package com.example.claptofindphone.service


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.claptofindphone.R
import com.example.claptofindphone.activity.MainActivity
import java.io.IOException


class MyService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private var recorder: MediaRecorder? = null
    private val channelId = "ChannelID"

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.song)
        Log.d(TAG, "on create called")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification()

        object : Thread() {
            override fun run() {
                recordAudio()
            }
        }.start()

        return START_STICKY
    }

    @SuppressLint("WrongConstant")
    private fun showNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Audio Service",
                NotificationManager.IMPORTANCE_NONE
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
        }

        val mainIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Recorder")
//            .setContentText("this is my notification")
            .setSmallIcon(R.mipmap.ic_launcher)

            .setContentIntent(pendingIntent)
            .build()

        startForeground(123, notification)
    }


    private fun recordAudio() {

        var clapDetectedNumber = 1

        recorder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder!!.setOutputFile(externalMediaDirs.first().absolutePath + "/music.3gp")
        var startAmplitude = 0
        var finishAmplitude: Int
        val amplitudeThreshold = 18000
        var counter = 0
        try {
            recorder!!.prepare()
            recorder!!.start()
            startAmplitude = recorder!!.maxAmplitude
        } catch (e: IOException) {
            e.printStackTrace()
        }
        while (clapDetectedNumber <= 1) {
            counter++
            waitSome()
            finishAmplitude = recorder!!.maxAmplitude
            Log.d(TAG, "recordAudio: $finishAmplitude")
            if (finishAmplitude >= amplitudeThreshold) {
                clapDetectedNumber++
                playAudio()
                done()
//                stopForeground(true)
//                stopSelf()
            }
        }
    }

    private fun done() {
        if (recorder != null) {
            recorder!!.stop()
            recorder!!.release()
        }
    }

    private fun waitSome() {
        try {
            Thread.sleep(250)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playAudio() {


        try {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        //Toast.makeText(this, "Audio Started playing", Toast.LENGTH_SHORT).show()
    }

    private fun stopAudio() {
        try {

            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer.release()
            } else {
                // Toast.makeText(this, "Audio has not played", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        stopAudio()
        super.onDestroy()

    }

}