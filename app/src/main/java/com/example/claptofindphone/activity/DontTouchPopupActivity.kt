package com.example.claptofindphone.activity

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.claptofindphone.R
import com.example.claptofindphone.database.ToneDatabase
import com.example.claptofindphone.databinding.ActivityDontTouchPopupBinding
import com.example.claptofindphone.service.TouchService
import com.example.claptofindphone.utils.ByteArrayMediaDataSource
import com.example.claptofindphone.utils.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DontTouchPopupActivity : AppCompatActivity() {
    lateinit var binding: ActivityDontTouchPopupBinding
    private lateinit var mPlayer: MediaPlayer
    private lateinit var dataStoreRepo: DataStoreRepository
    private lateinit var audioManager: AudioManager
    private var isFlashing = false
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityDontTouchPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataStoreRepo = DataStoreRepository(this)

        CoroutineScope(Dispatchers.IO).launch {
            isFlashing = dataStoreRepo.getTouchModeFlashValue.first()

        }
        val database = ToneDatabase.getDatabase(application)
        scope.launch {
            val tone = database.toneDao().getSelectedTone()
            mPlayer = MediaPlayer()
            mPlayer.setDataSource(ByteArrayMediaDataSource(tone.bytes))
            mPlayer.prepare()
            mPlayer.start()
        }

//        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
//       audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0)
//        getSystemService(AUDIO_SERVICE) as AudioManager



        binding.ivPopup.setOnClickListener {
                mPlayer.stop()
            finish()
        }



    }

    private fun flashOn() {
        object : Thread() {
            override fun run() {
                var pattern = true
                var isFlash = false
                val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraListId = cameraManager.cameraIdList[0]
                while (isFlashing) {
                    if (pattern) {
                        try {
                            cameraManager.setTorchMode(cameraListId, true)
                            isFlash = pattern
                        } catch (e: Exception) {
                            Log.d("Exception", "flash on : ${e.message} ")
                        }
                    } else {
                        try {
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
                        cameraManager.setTorchMode(cameraListId, false)
                    } catch (e: Exception) {
                        Log.d("Exception", "flash off: ${e.message} ")
                    }
                }
            }
        }.start()
    }
}
