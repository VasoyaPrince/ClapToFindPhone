package com.example.claptofindphone.service

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.example.claptofindphone.R
import com.example.claptofindphone.activity.MainActivity
import com.example.claptofindphone.activity.PopupActivity
import com.example.claptofindphone.database.ToneDatabase
import com.example.claptofindphone.utils.ByteArrayMediaDataSource
import com.example.claptofindphone.utils.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs


class AudioService : Service() {
    companion object{
        var serviceStop = MutableLiveData<Boolean>()
    }

    private lateinit var record: AudioRecord
    private lateinit var mediaPlayer: MediaPlayer
    private var isRecording: Boolean = false
    private var recordState: Int = 0
    private var minBuffer: Int = 0
    private val channelId = "ChannelID"
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var dataStoreRepo: DataStoreRepository
    private var isFlashOn: Boolean = false
    private var isFlashing: Boolean = true
    private var isVibrateOn: Boolean = false
    private var isVibrating: Boolean = true
    private var isSmartModeOn: Boolean = false
    private lateinit var vibrationEffect: VibrationEffect
    private lateinit var vibrator: Vibrator
    private var clapFind: Int = 0
     var  featureType : Int = 0


    override fun onCreate() {
        super.onCreate()
        val database = ToneDatabase.getDatabase(application)
        dataStoreRepo = DataStoreRepository(this)



        scope.launch {
            val tone = database.toneDao().getSelectedTone()
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(ByteArrayMediaDataSource(tone.bytes))
            mediaPlayer.prepareAsync()
            isFlashOn = dataStoreRepo.getFlashValue.first()
            isVibrateOn = dataStoreRepo.getVibrateValue.first()
            isSmartModeOn = dataStoreRepo.getSmartModeValue.first()
        }

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification()
        recordVoice()
         featureType =  intent!!.getIntExtra("featureType",0)
        object : Thread() {
            override fun run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startAudio()
                }
            }
        }.start()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun recordVoice() {
        val source = MediaRecorder.AudioSource.CAMCORDER
        val channelIn: Int = AudioFormat.CHANNEL_IN_MONO
        val format: Int = AudioFormat.ENCODING_PCM_16BIT
        val sampleRate: Int = getSampleRate()
        isRecording = false

        minBuffer = AudioRecord.getMinBufferSize(sampleRate, channelIn, format)

        record = AudioRecord(source, sampleRate, channelIn, format, minBuffer)
        recordState = record.state

    }


    fun startAudio() {
        var read: Int
        if (recordState == AudioRecord.STATE_INITIALIZED) {
            record.startRecording()
            isRecording = true
            Log.d("Record", "Recording...")
        }
        clapFind = 0
        //it is locked
        while (isRecording) {
            val audioData = ShortArray(minBuffer)
            read = record.read(audioData, 0, minBuffer)
            Log.d("Record", "Read: $read")
            var amplitude = 0
            if (read < 0) {
                amplitude = 0
            }

            var sum = 0
            for (i in 0 until read) {
                sum += abs(audioData[i].toInt())
            }

            if (read > 0) {
                amplitude = sum / read
            }
            Log.d(TAG, "amplitude: $amplitude")
            if (featureType == 0){
                Log.d("TAG", "Clap to find")
                if (amplitude >= 2880) {
                    //clap detected
                    if (isSmartModeOn) {
                        val myKM = (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager)
                        if (!(myKM.isKeyguardLocked)) {
                            continue
                        }
                    }
                    clapFind++
                    if (clapFind == 2) {
                        playAudio()
                        if (isFlashOn) {
                            flashOn()
                        }
                        if (isVibrateOn) {
                            vibrateOn()
                        }
                        endAudioRecording()
                        val alarmIntent = Intent(this, PopupActivity::class.java)
                        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        this.startActivity(alarmIntent)
                    }
                }
            }else{
                Log.d("TAG", "whistle to find")
                if (amplitude >= 2800) {

                    if (isSmartModeOn) {
                        val myKM = (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager)
                        if (!(myKM.isKeyguardLocked)) {
                            continue
                        }
                    }
                    clapFind++
                    if (clapFind == 2) {
                        playAudio()
                        if (isFlashOn) {
                            flashOn()
                        }
                        if (isVibrateOn) {
                            vibrateOn()
                        }
                        endAudioRecording()
                        val alarmIntent = Intent(this, PopupActivity::class.java)
                        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        this.startActivity(alarmIntent)
                    }
                }
            }

        }
    }


    private fun endAudioRecording() {
        if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) record.stop()
        isRecording = false
        Log.d("Record", "Stopping...")
    }

    override fun onDestroy() {
        if (isRecording) {
            endAudioRecording()
        }
        if (clapFind == 2) {
            if (isFlashOn) {
                flashOff()
            }
            if (isVibrateOn) {
                vibrationOff()
            }
            stopAudio()
        }
        serviceStop.postValue(false)
        super.onDestroy()

    }


    private fun getSampleRate(): Int {
        val channelIn: Int = AudioFormat.CHANNEL_IN_MONO
        val format: Int = AudioFormat.ENCODING_PCM_16BIT

        //Find a sample rate that works with the device
        for (rate in intArrayOf(8000, 11025, 16000, 22050, 44100, 48000)) {
            val buffer = AudioRecord.getMinBufferSize(rate, channelIn, format)
            if (buffer > 0) return rate
        }
        return -1
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
    }

    private fun stopAudio() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer.release()
            } else {
                //Toast.makeText(this, "Audio has not played", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun flashOff() {
        isFlashing = false
    }

    private fun vibrationOff() {
        isVibrating = false
        vibrator.cancel()

    }

    private fun flashOn() {
        object : Thread() {
            override fun run() {
                var pattern = true
                var isFlash = false
                val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                while (isFlashing) {
                    if (pattern) {
                        try {
                            val cameraListId = cameraManager.cameraIdList[0]
                            cameraManager.setTorchMode(cameraListId, true)
                            isFlash = pattern
                        } catch (e: Exception) {
                            Log.d("Exception", "flash on : ${e.message} ")
                        }
                    } else {
                        try {
                            val cameraListId = cameraManager.cameraIdList[0]
                            cameraManager.setTorchMode(cameraListId, false)
                            isFlash = pattern
                        } catch (e: Exception) {
                            Log.d("Exception", "flash off: ${e.message} ")
                        }
                    }
                    pattern = !pattern
                    try {
                        sleep(50)
                    } catch (e: Exception) {
                        Log.d("Exception", "Thread: ${e.message} ")
                    }

                }
                if (isFlash) {
                    try {
                        val cameraListId = cameraManager.cameraIdList[0]
                        cameraManager.setTorchMode(cameraListId, false)
                    } catch (e: Exception) {
                        Log.d("Exception", "flash off: ${e.message} ")
                    }
                }
            }
        }.start()
    }

    @SuppressLint("WrongConstant")
    private fun vibrateOn() {

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timing = longArrayOf(0, 100, 20, 200, 20, 5000)
            vibrationEffect = VibrationEffect.createWaveform(timing, 0)
            vibrator.vibrate(vibrationEffect)
        }
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
            .setContentTitle("Clap To Find")
            .setContentText("Service running in background...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(123, notification)
    }

}