package com.example.claptofindphone.activity


import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.claptofindphone.R
import com.example.claptofindphone.adapter.InfoAdapter
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.utils.Utils
import com.example.claptofindphone.databinding.ActivityInfoBinding
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import java.lang.reflect.Array

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding
    private var mInterstitialAd: InterstitialAd? = null
    private var count: Int = 0
    private val testMode = true
    private lateinit var item : ArrayList<String>

    private lateinit var loadListener: IUnityAdsLoadListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.info_title_layout)

        supportActionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayHomeAsUpEnabled(true)
        }



             item = arrayListOf(
                " To enable this feature , ''click the  Find My Phone''  button.",
                "Turn on the Mike . This feature is now available for you .",
                " Click \" Select Tone \" to choose your preferred tone from Audio Settings .",
                "Flash on/off when lost your mobile phone ",
                "vibrate on/off when lost your mobile phone ",
                "smart mode is a Alert screen when phone locked "
            )


        val adapter = InfoAdapter(this, item)
        binding.infoRecyclerView.adapter = adapter

        //native ads
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

        //interstitial ads
        loadInterstitialAds()
        MobileAds.initialize(this)

        //Unity Ads
        if (Value.unityAds.unityGameId.isNotEmpty()) {
            UnityAds.initialize(this, Value.unityAds.unityGameId, testMode)
        }

        loadListener = object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(p0: String?) {
                // UnityAds.show(this@InfoActivity, adUnitId,showListener())
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
            UnityAds.load(Value.unityAds.interstitialAd, loadListener)
        }
    }

    override fun onBackPressed() {
        Value.ClapToFindPhone.isBackPress++
        if (Utils.isNetworkConnected(this)) {
            if (Value.ClapToFindPhone.isBackPress == 3) {

                UnityAds.show(this@InfoActivity, Value.unityAds.interstitialAd, showListener())
                super.onBackPressed()
                Value.ClapToFindPhone.isBackPress = 0
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

    private fun loadInterstitialAds() {
        val adRequest: AdRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            Value.googleAds.interstitialAd,
            adRequest,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(InterstitialAd: InterstitialAd) {
                    super.onAdLoaded(InterstitialAd)
                    mInterstitialAd = InterstitialAd
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.d("onAdFailedToLoad", "onAdFailedToLoad: $p0")
                    mInterstitialAd = null
                }

            })
    }

    private fun showInterstitialAds() {
        if (mInterstitialAd != null) {

            if (count != 4) {
                object : FullScreenContentCallback() {

                    override fun onAdFailedToShowFullScreenContent(AdError: AdError) {
                        super.onAdFailedToShowFullScreenContent(AdError)
                        Log.d("mInterstitialAd", "onAdFailedToShowFullScreenContent: $AdError")
                        mInterstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        mInterstitialAd = null
                        loadInterstitialAds()
                    }
                }.also { mInterstitialAd!!.fullScreenContentCallback = it }
                mInterstitialAd!!.show(this)
                count++
                Log.d("count", "showInterstitialAds: $count")
            }

        } else {
            Toast.makeText(this, "ads failed", Toast.LENGTH_SHORT).show()
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
                UnityAds.load(Value.unityAds.interstitialAd, loadListener)
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


        }
    }
}