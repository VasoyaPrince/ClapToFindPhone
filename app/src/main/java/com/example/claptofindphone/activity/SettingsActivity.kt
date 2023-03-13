package com.example.claptofindphone.activity


import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.ActivitySettingsBinding
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.utils.DataStoreRepository
import com.example.claptofindphone.utils.Utils
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity(), IUnityAdsInitializationListener {

    private lateinit var binding: ActivitySettingsBinding
    private var isFlashOn: Boolean = false
    private var isVibrateOn: Boolean = false
    private var isSmartMode: Boolean = false
    private lateinit var dataStoreRepo: DataStoreRepository
    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedAd: RewardedAd? = null
    private val testMode = true
    private var connectivity: ConnectivityManager? = null
    private lateinit var loadListener: IUnityAdsLoadListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.settings_title_layout)

        dataStoreRepo = DataStoreRepository(this)

        connectivity = this.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager

        val adRequest = AdRequest.Builder().build()

        loadListener = object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(p0: String?) {

            }

            override fun onUnityAdsFailedToLoad(
                p0: String?,
                p1: UnityAds.UnityAdsLoadError?,
                p2: String?
            ) {

            }
        }

        if (Value.unityAds.unityGameId.isNotEmpty()) {
            UnityAds.initialize(this, Value.unityAds.unityGameId, testMode, this)
        }

        //native ads
        MobileAds.initialize(this)
        val adLoader: AdLoader = AdLoader.Builder(this, Value.googleAds.nativeAd)
            .forNativeAd { nativeAd ->
                val styles =
                    NativeTemplateStyle.Builder().build()
                val template = findViewById<TemplateView>(R.id.my_template)
                template.setStyles(styles)
                template.setNativeAd(nativeAd)
            }
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

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

        fun rewardedAd() {

            RewardedAd.load(
                this,
                Value.googleAds.rewardedAd,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d(TAG, adError.message)
                        mRewardedAd = null
                    }

                    override fun onAdLoaded(rewardedAd: RewardedAd) {
                        Log.d(TAG, "Ad was loaded.")
                        mRewardedAd = rewardedAd
                    }
                })
        }

        supportActionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.setToneCardView.setOnClickListener {

            startActivity(Intent(this, TonsActivity::class.java))
        }

        CoroutineScope(Dispatchers.IO).launch {
            binding.setFlashSwitch.isChecked = dataStoreRepo.getFlashValue.first()
            binding.setFlashSwitch.setOnCheckedChangeListener { _, isChecked ->
                isFlashOn = isChecked
                if (isChecked) {
                    if (Utils.isNetworkConnected(this@SettingsActivity)) {
                        displayRewardedAd()
                    }
                }

                CoroutineScope(Dispatchers.IO).launch {
                    dataStoreRepo.storeFlashValue(isFlashOn)
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            binding.setSwitchVibrate.isChecked = dataStoreRepo.getVibrateValue.first()
            binding.setSwitchVibrate.setOnCheckedChangeListener { _, isChecked ->
                isVibrateOn = isChecked
                if (isChecked) {
                    if (Utils.isNetworkConnected(this@SettingsActivity)) {
                        mRewardedAd?.show(this@SettingsActivity) {}
                        displayRewardedAd()
                    }
                }
                CoroutineScope(Dispatchers.IO).launch {
                    dataStoreRepo.storeVibrateValue(isVibrateOn)
                }
                //google ads
                //rewardedAd()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            binding.setSwitchSmartMode.isChecked = dataStoreRepo.getSmartModeValue.first()
            binding.setSwitchSmartMode.setOnCheckedChangeListener { _, isChecked ->
                isSmartMode = isChecked
                if (isChecked) {
                    if (Utils.isNetworkConnected(this@SettingsActivity)) {
                        displayRewardedAd()
                    }
                }

                CoroutineScope(Dispatchers.IO).launch {
                    dataStoreRepo.storeSmartModeValue(isSmartMode)
                }
            }
        }

        mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad was shown.")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.d(TAG, "Ad failed to show.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad was dismissed.")
                mRewardedAd = null
            }
        }
    }

    private fun showListener() = object : IUnityAdsShowListener {
        override fun onUnityAdsShowFailure(
            p0: String?,
            p1: UnityAds.UnityAdsShowError?,
            p2: String?
        ) {
            Log.e("UnityAdsExample", "Unity Ads failed to show ad for $p0 with error: [$p1] $p2")
        }

        override fun onUnityAdsShowStart(p0: String?) {
            Log.v("UnityAdsExample", "onUnityAdsShowStart: $p0")
        }

        override fun onUnityAdsShowClick(p0: String?) {
            Log.v("UnityAdsExample", "onUnityAdsShowClick: $p0")
        }

        override fun onUnityAdsShowComplete(
            p0: String?,
            p1: UnityAds.UnityAdsShowCompletionState?
        ) {
            Log.v("UnityAdsExample", "onUnityAdsShowComplete: $p0")
        }
    }


    private fun displayRewardedAd() {
        if (UnityAds.isInitialized()) {
            UnityAds.show(this, Value.unityAds.rewardedAd, showListener())
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        Value.SettingsBackPress.isBackPress++
        if (Utils.isNetworkConnected(this)) {
            if (Value.SettingsBackPress.isBackPress == 3) {
                if (UnityAds.isInitialized()) {
                    UnityAds.show(this@SettingsActivity, Value.unityAds.rewardedAd, showListener())
                    Value.SettingsBackPress.isBackPress = 0
                    super.onBackPressed()
                } else {

                    UnityAds.initialize(this, Value.unityAds.unityGameId, testMode, this)

                }
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onInitializationComplete() {
        UnityAds.load(Value.unityAds.rewardedAd, loadListener)
    }

    override fun onInitializationFailed(p0: UnityAds.UnityAdsInitializationError?, p1: String?) {

    }


}