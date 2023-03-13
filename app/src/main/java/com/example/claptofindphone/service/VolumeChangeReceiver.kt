package com.example.claptofindphone.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class VolumeChangeReceiver(val onVolumeChanged: (volume: Int) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals("android.media.VOLUME_CHANGED_ACTION")) {
            val newVolume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0)
            val oldVolume = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", 0)
            if (newVolume != oldVolume) {
                onVolumeChanged(newVolume)
            }
        }
    }
}