package com.example.claptofindphone.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.ActivityPinBinding
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.utils.DataStoreRepository
import com.example.claptofindphone.utils.Utils
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.*
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PinActivity : AppCompatActivity() {
    lateinit var binding: ActivityPinBinding
    private lateinit var dataStoreRepo: DataStoreRepository
    private val testMode = true
    private lateinit var unityLoadListener: IUnityAdsLoadListener
    private lateinit var unityLoadListener2: IUnityAdsLoadListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.title_pinset_layout)
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


        unityLoadListener2 = object : IUnityAdsLoadListener {
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
            UnityAds.load(Value.unityAds.interstitialAd, unityLoadListener2)
        }



        dataStoreRepo = DataStoreRepository(this)
        CoroutineScope(Dispatchers.IO).launch {
            binding.setPINMode.isChecked = dataStoreRepo.getPinSetValue.first()
            binding.setPINMode.setOnCheckedChangeListener { p, isChecked ->
                p.isChecked = false
                if (isChecked) {
                    CoroutineScope(Dispatchers.IO).launch {
                        dataStoreRepo.storePinSetValue(isChecked)
                        startActivity(Intent(this@PinActivity, ShowPinActivity::class.java))
                        finish()
                    }
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        dataStoreRepo.storePinSetValue(isChecked)
                        //displayRewardedAd()
                    }
                }

            }
        }


    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        Value.PinMode.isBackPress++
        if (Utils.isNetworkConnected(this)) {
            if (Value.PinMode.isBackPress == 3) {

                UnityAds.show(this@PinActivity, Value.unityAds.interstitialAd, showListener())
                super.onBackPressed()
                Value.PinMode.isBackPress = 0
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