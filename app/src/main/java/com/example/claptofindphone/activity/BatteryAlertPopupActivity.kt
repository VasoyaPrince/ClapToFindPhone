package com.example.claptofindphone.activity

import android.app.KeyguardManager
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.claptofindphone.R
import com.example.claptofindphone.database.ToneDatabase
import com.example.claptofindphone.databinding.ActivityBatteryAlertBinding
import com.example.claptofindphone.databinding.ActivityBattreyAlertPopupBinding
import com.example.claptofindphone.databinding.ActivityChargerPopupBinding
import com.example.claptofindphone.utils.ByteArrayMediaDataSource
import com.example.claptofindphone.utils.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BatteryAlertPopupActivity : AppCompatActivity() {
    lateinit var binding: ActivityBattreyAlertPopupBinding
    private var mPlayer: MediaPlayer? = null
    private lateinit var dataStoreRepo: DataStoreRepository
    private val job = SupervisorJob()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)


        binding = ActivityBattreyAlertPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val database = ToneDatabase.getDatabase(application)
        dataStoreRepo = DataStoreRepository(this)

        CoroutineScope(Dispatchers.IO + job ).launch {
            val tone = database.toneDao().getSelectedTone()
            mPlayer = MediaPlayer()
            mPlayer!!.setDataSource(ByteArrayMediaDataSource(tone.bytes))
            mPlayer!!.prepare()
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            mPlayer?.start()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {

            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        binding.ivPopup.setOnClickListener {
            mPlayer?.stop()
            //startActivity(Intent(this,BatteryAlertActivity::class.java))
            finish()
        }


    }
}