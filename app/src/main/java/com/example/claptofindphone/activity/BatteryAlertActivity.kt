package com.example.claptofindphone.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.ActivityBatteryAlertBinding
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.utils.Utils
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.*
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds

class BatteryAlertActivity : AppCompatActivity() {
    lateinit var binding: ActivityBatteryAlertBinding
    private var receiver: BroadcastReceiver? = null
    private var progress1: Int = 0
    private var mPlayer: MediaPlayer? = null
    private val testMode = true
    private lateinit var unityLoadListener: IUnityAdsLoadListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBatteryAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.title_batteryalert_layout)

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
                    // Handle the failure by logging, altering the UI, and so on.
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

        mPlayer = MediaPlayer.create(this, R.raw.song2)
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0)

        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val per = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        //Log.d("TAG", "BATTERY_PROPERTY_CAPACITY: $per")

        val percentage = binding.percentage
        binding.setBatteryPercentage.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, Progress: Int, fromUser: Boolean) {

                //Log.d("TAG", "BATTERY: $batLevel")
                progress1 = Progress
                percentage.text = "$Progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress = per
                val bm = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager
                val batLevel: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                seekBar?.progress = batLevel

                Log.d("TAG", "BATTERY_PROPERTY_CAPACITY: ${seekBar?.progress}")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // seekBar?.progress = per
                // Log.d("TAG", "BATTERY_PROPERTY_CAPACITY: ${seekBar?.progress}")
            }

        })
        percentage.text
        //val  percentageInt  = Integer.parseInt(percentage.text.toString())

        binding.setBatteryMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                UnityAds.show(this, Value.unityAds.interstitialAd, showListener2())
            } else {
                if (receiver != null) {
                    mPlayer?.stop()
                    binding.setBatteryMode.isChecked = false
                    unregisterReceiver(receiver)
                }
                binding.setBatteryPercentage.isEnabled = true
            }

        }

    }

    override fun onBackPressed() {
        Value.BatteryAlertMode.isBackPress++
        if (Utils.isNetworkConnected(this)) {
            if (Value.BatteryAlertMode.isBackPress == 3) {

                UnityAds.show(
                    this@BatteryAlertActivity,
                    Value.unityAds.interstitialAd,
                    showListener()
                )
                super.onBackPressed()
                Value.BatteryAlertMode.isBackPress = 0
                //showInterstitialAds()
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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

    private fun showListener2(): IUnityAdsShowListener = object : IUnityAdsShowListener {
        override fun onUnityAdsShowFailure(
            placementId: String?,
            error: UnityAds.UnityAdsShowError?,
            message: String?
        ) {
            if (UnityAds.isInitialized()) {
                UnityAds.load(Value.unityAds.interstitialAd, unityLoadListener)
            }

            binding.setBatteryPercentage.isEnabled = false
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val integerBatteryLevel =
                        intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    if (integerBatteryLevel == progress1) {
                        //mPlayer?.start()
                        unregisterReceiver(receiver)
                        startActivity(
                            Intent(
                                this@BatteryAlertActivity,
                                BatteryAlertPopupActivity::class.java
                            )
                        )
                        finish()
                    }
                }

            }
            registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

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
            binding.setBatteryPercentage.isEnabled = false
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val integerBatteryLevel =
                        intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    if (integerBatteryLevel == progress1) {
                        //mPlayer?.start()
                        unregisterReceiver(receiver)
                        startActivity(
                            Intent(
                                this@BatteryAlertActivity,
                                BatteryAlertPopupActivity::class.java
                            )
                        )
                        finish()
                    }
                }

            }
            registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))


            if (UnityAds.isInitialized()) {
                UnityAds.load(Value.unityAds.interstitialAd, unityLoadListener)
            }
        }
    }
}