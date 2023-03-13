package com.example.claptofindphone.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.BatteryManager
import androidx.core.content.ContextCompat.startActivity
import com.example.claptofindphone.R
import com.example.claptofindphone.activity.ChargerPopupActivity

class ChargerBroadcastReceiver : BroadcastReceiver() {
    var chargerFlag = 0
    var chargerFlag1 = 0
    private var chargerFlag2 = 0
    private var mPlayer: MediaPlayer? = null


    override fun onReceive(context: Context?, intent: Intent?) {

        mPlayer = MediaPlayer.create(context, R.raw.song2)
        val audioManager = context?.getSystemService(AUDIO_SERVICE) as AudioManager

        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        if (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB) {
            chargerFlag = 1
        } else if (plugged == 0) {
            chargerFlag1 = 1
            chargerFlag = 0
            if (chargerFlag == 0 && chargerFlag1 == 1 && chargerFlag2 == 1) {
                chargerFlag2 = 0
                mPlayer?.start()
                    //startActivity(Intent(this, ChargerPopupActivity::class.java))
            }
        }
    }
}