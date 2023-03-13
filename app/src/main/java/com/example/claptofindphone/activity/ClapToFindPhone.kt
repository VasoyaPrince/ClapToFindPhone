package com.example.claptofindphone.activity


import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.ActivityClaptofindphoneBinding
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.service.AudioService
import com.example.claptofindphone.service.AudioService.Companion.serviceStop
import com.example.claptofindphone.service.TouchService
import com.example.claptofindphone.utils.Utils
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds


class ClapToFindPhone : AppCompatActivity() {

    private lateinit var binding: ActivityClaptofindphoneBinding
    private val recordAudio = 0
    private var isServiceStarted: Boolean = false
    private var mInterstitialAd: InterstitialAd? = null
    private val testMode = true
    private val adUnitId = Value.unityAds.interstitialAd
    private lateinit var unityLoadListener: IUnityAdsLoadListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClaptofindphoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.title_clapfindphone_layout)
        supportActionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayHomeAsUpEnabled(true)
        }

        val featureType: Int = intent.extras!!.getInt("type")

        serviceStop.observe(this) {
            binding.btnMike.setImageResource(R.drawable.voice)
        }

        Log.d(TAG, "onCreate: $featureType")

        //InterstitialAd
        val adRequest = AdRequest.Builder().build()

        //checkOverlayPermission()

        InterstitialAd.load(this, Value.googleAds.interstitialAd, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    mInterstitialAd = null
                }


                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                }

            })

        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed.")
                super.onAdDismissedFullScreenContent()
                // mInterstitialAd?.fullScreenContentCallback

            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Log.d(TAG, "Ad failed to show.")
                super.onAdFailedToShowFullScreenContent(p0)
                mInterstitialAd?.fullScreenContentCallback
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
                mInterstitialAd = null
            }
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
            UnityAds.load(adUnitId, unityLoadListener)
        }

        val intent = Intent(this@ClapToFindPhone, AudioService::class.java)
        intent.putExtra("featureType", featureType)
        if (isServiceRunningInForeground(AudioService::class.java)) {
            isServiceStarted = true
            binding.btnMike.setImageResource(R.drawable.mute)
        } else {
            isServiceStarted = false
            binding.btnMike.setImageResource(R.drawable.voice)
        }

        binding.btnMike.setOnClickListener {
            if (isServiceStarted) {
                binding.btnMike.setImageResource(R.drawable.voice)
                stopService(intent)
                //Ads Show
                mInterstitialAd?.show(this)

            } else {
                if (!Settings.canDrawOverlays(this)) {

                    val builder: AlertDialog.Builder =
                        AlertDialog.Builder(this).setMessage("Draw Over Other Apps")
                            .setCancelable(false)
                            .setPositiveButton(
                                "Yes"
                            ) { dialog, which ->

                                val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                startActivity(myIntent)
                            }
                            .setNegativeButton(
                                "No"
                            ) { dialog, which ->
                                dialog?.cancel()
                            }

                    val alert = builder.create()
                    alert.setIcon(R.mipmap.ic_launcher_round)
                    alert.setTitle("Permission")
                    alert.show()
                }

                if (ActivityCompat.checkSelfPermission(
                        this@ClapToFindPhone,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@ClapToFindPhone,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        recordAudio
                    )
                    return@setOnClickListener
                } else {
                    binding.btnMike.setImageResource(R.drawable.mute)
                    startService(intent)
                }
            }
            isServiceStarted = !isServiceStarted
        }

        binding.infoImageView.setOnClickListener {

            val intent = Intent(this@ClapToFindPhone, InfoActivity::class.java)

            startActivity(intent)

        }

        binding.settingsImageView.setOnClickListener {
            startActivity(Intent(this@ClapToFindPhone, SettingsActivity::class.java))
        }

    }

    private fun isServiceRunningInForeground(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    override fun onBackPressed() {

        Value.InfoBackPress.isBackPress++
        if (Utils.isNetworkConnected(this)) {
            if (Value.InfoBackPress.isBackPress == 4) {

                UnityAds.show(this@ClapToFindPhone, Value.unityAds.interstitialAd, showListener())
                super.onBackPressed()
                Value.InfoBackPress.isBackPress = 0
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
                UnityAds.load(adUnitId, unityLoadListener)
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

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            // send user to the device settings
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(myIntent)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}

