package com.example.claptofindphone.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.ActivityAudioBinding
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.service.VolumeChangeReceiver
import com.example.claptofindphone.utils.Utils
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.*
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import kotlin.math.ceil
import kotlin.math.roundToInt

class AudioActivity : AppCompatActivity() {
    lateinit var binding: ActivityAudioBinding
    private var progress1: Int = 0
    lateinit var mAudio: AudioManager
    private val testMode = true
    private lateinit var unityLoadListener: IUnityAdsLoadListener
    private lateinit var broadCast: VolumeChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.title_audio_layout)
        supportActionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayHomeAsUpEnabled(true)
        }

        //native ads

        MobileAds.initialize(this)
        val adLoader1: AdLoader = AdLoader.Builder(this, Value.googleAds.nativeAd)
            .forNativeAd { nativeAd ->
                val styles =
                    NativeTemplateStyle.Builder().build()
                val template = binding.myTemplate2
                template.setStyles(styles)
                template.setNativeAd(nativeAd)
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    //Toast.makeText(this@ClapToFindPhone, "failed", Toast.LENGTH_SHORT).show()
                }
            })
            .build()

        adLoader1.loadAd(AdRequest.Builder().build())

        //Unity Ads
        if (Value.unityAds.unityGameId.isNotEmpty()) {
            UnityAds.initialize(this, Value.unityAds.unityGameId, testMode)
        }

        unityLoadListener = object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(p0: String?) {
                //UnityAds.show(this@InfoActivity, adUnitId,showListener())
            }

            override fun onUnityAdsFailedToLoad(
                p0: String?,
                p1: UnityAds.UnityAdsLoadError?,
                p2: String?
            ) {
                Log.e(
                    "UnityAdsExample",
                    "Unity Ads failed to load ad for $p0 with error: [$p1] $p2"
                )
            }

        }

        if (UnityAds.isInitialized()) {
            UnityAds.load(Value.unityAds.interstitialAd, unityLoadListener)
        }



        mAudio = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
        (AudioManager.STREAM_RING)


        initControls()

        binding.setToneCardView.setOnClickListener {

            startActivity(Intent(this, TonsActivity::class.java))
        }
    }

    private fun initControls() {

        broadCast = VolumeChangeReceiver { volume ->
            setProgress()
        }
        registerReceiver(broadCast, IntentFilter("android.media.VOLUME_CHANGED_ACTION"))
        binding.setVolumePercentage.max = mAudio
            .getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        setProgress()
        binding.setVolumePercentage.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mAudio.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }

    private fun setProgress()  {
        val mediaVolume: Int = mAudio.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVol: Int = mAudio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volPercentage = ceil(mediaVolume.toDouble() / maxVol.toDouble() * 100)
        binding.setVolumePercentage.progress = mediaVolume
        binding.percentage.text = "${volPercentage.roundToInt()} %"
    }

    override fun onDestroy() {
        unregisterReceiver(broadCast)
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        Value.AudioMode.isBackPress++
        if (Utils.isNetworkConnected(this)) {
            if (Value.AudioMode.isBackPress == 3) {

                UnityAds.show(this@AudioActivity, Value.unityAds.interstitialAd, showListener())
                super.onBackPressed()
                Value.AudioMode.isBackPress = 0
                //showInterstitialAds()
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }


    private fun showListener(): IUnityAdsShowListener = object : IUnityAdsShowListener {
        override fun onUnityAdsShowFailure(
            placementId: String?,
            error: UnityAds.UnityAdsShowError?,
            message: String?
        ) {
            Log.e(
                "UnityAdsExample",
                "Unity Ads failed to show ad for $placementId with error: [$error] $message"
            )
            if (UnityAds.isInitialized()) {
                UnityAds.load(Value.unityAds.interstitialAd, unityLoadListener)
            }
        }

        override fun onUnityAdsShowStart(placementId: String?) {
            Log.v("UnityAdsExample", "onUnityAdsShowStart: $placementId")
        }

        override fun onUnityAdsShowClick(placementId: String?) {
            Log.v("UnityAdsExample", "onUnityAdsShowClick: $placementId")
        }

        override fun onUnityAdsShowComplete(
            placementId: String?,
            p1: UnityAds.UnityAdsShowCompletionState?
        ) {
            Log.v("UnityAdsExample", "onUnityAdsShowComplete: $placementId")
            finish()
        }
    }
}