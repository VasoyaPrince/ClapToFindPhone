package com.example.claptofindphone.activity

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.claptofindphone.databinding.ActivitySplashBinding
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.utils.Utils
import com.example.claptofindphone.model.MainViewModel
import com.example.claptofindphone.utils.ApiState
import com.example.claptofindphone.utils.DataStoreRepository
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(), IUnityAdsInitializationListener {

    lateinit var binding: ActivitySplashBinding
    private val testMode = true
    private lateinit var loadListener: IUnityAdsLoadListener
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var dataStoreRepo: DataStoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataStoreRepo = DataStoreRepository(this)
        //Api Call (Ads ID )

        Handler(Looper.getMainLooper()).postDelayed({

            if (Utils.isNetworkConnected(this@SplashActivity)) {

                mainViewModel.getPostFlow(application.packageName)
                lifecycleScope.launchWhenCreated {
                    mainViewModel.apiStateFlow.collect {
                        when (it) {

                            is ApiState.SuccessResponse -> {

                                for (i in 0 until it.response?.data?.adsProvider!!.size) {
                                    if (it.response.data.adsProvider[i].name == "Google Adsense") {
                                        for (j in 0 until it.response.data.adsProvider[i].fields.size) {

                                            when (it.response.data.adsProvider[i].fields[j].name) {

                                                "Application Id" -> {
                                                    Value.googleAds.appId =
                                                        it.response.data.adsProvider[i].fields[j].value
                                                }
                                                "Interstitial Ad" -> {
                                                    Value.googleAds.interstitialAd =
                                                        it.response.data.adsProvider[i].fields[j].value
                                                }
                                                "Native Ad" -> {
                                                    Value.googleAds.nativeAd =
                                                        it.response.data.adsProvider[i].fields[j].value
                                                }
                                                "Rewarded Ad" -> {
                                                    Value.googleAds.rewardedAd =
                                                        it.response.data.adsProvider[i].fields[j].value
                                                }
                                            }

                                        }
                                    } else if (it.response.data.adsProvider[i].name == "Unity") {
                                        for (j in 0 until it.response.data.adsProvider[i].fields.size) {

                                            when (it.response.data.adsProvider[i].fields[j].name) {

                                                "Unity Game Id" -> {
                                                    Value.unityAds.unityGameId =
                                                        it.response.data.adsProvider[i].fields[j].value
                                                }
                                                "Rewarded Ad" -> {
                                                    Value.unityAds.rewardedAd =
                                                        it.response.data.adsProvider[i].fields[j].value
                                                }
                                                "Intertitial Ad" -> {
                                                    Value.unityAds.interstitialAd =
                                                        it.response.data.adsProvider[i].fields[j].value
                                                }
                                            }
                                        }
                                    }
                                }

                                loadListener = object : IUnityAdsLoadListener {
                                    override fun onUnityAdsAdLoaded(p0: String?) {
                                        if (Utils.isNetworkConnected(this@SplashActivity)) {
                                            UnityAds.show(
                                                this@SplashActivity,
                                                Value.unityAds.interstitialAd,
                                                showListener()
                                            )
                                            CoroutineScope(Dispatchers.IO).launch {
                                                val isPin = dataStoreRepo.getPinSetValue.first()
                                                if (isPin) {
                                                    startActivity(
                                                        Intent(
                                                            this@SplashActivity,
                                                            SplashPinShowActivity::class.java
                                                        )
                                                    )
                                                    finish()
                                                } else {
                                                    startActivity(
                                                        Intent(
                                                            this@SplashActivity,
                                                            MainActivity::class.java
                                                        )
                                                    )
                                                    finish()
                                                }
                                            }
                                        }

                                    }

                                    override fun onUnityAdsFailedToLoad(
                                        p0: String?,
                                        p1: UnityAds.UnityAdsLoadError?,
                                        p2: String?
                                    ) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val isPin = dataStoreRepo.getPinSetValue.first()
                                            if (isPin) {
                                                startActivity(
                                                    Intent(
                                                        this@SplashActivity,
                                                        SplashPinShowActivity::class.java
                                                    )
                                                )
                                                finish()
                                            } else {
                                                startActivity(
                                                    Intent(
                                                        this@SplashActivity,
                                                        MainActivity::class.java
                                                    )
                                                )
                                                finish()
                                            }
                                        }
                                    }
                                }
                                //Unity Ads
                                if (Value.unityAds.unityGameId.isNotEmpty()) {
                                    UnityAds.initialize(
                                        this@SplashActivity,
                                        Value.unityAds.unityGameId,
                                        testMode,
                                        this@SplashActivity
                                    )
                                }

                            }

                            is ApiState.FailureResponse -> {
                                Log.d(TAG, "${it.error.message}")
                            }
                            else -> {
                                Log.d("failed", "onCreate: ")
                            }
                        }
                    }
                }

            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val isPin = dataStoreRepo.getPinSetValue.first()
                    if (isPin) {
                        startActivity(
                            Intent(
                                this@SplashActivity,
                                SplashPinShowActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        startActivity(
                            Intent(
                                this@SplashActivity,
                                MainActivity::class.java
                            )
                        )
                        finish()
                    }
                }
            }

        },1000)


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
            //UnityAds.load(Value.unityAds.interstitialAd, loadListener)

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

    override fun onInitializationComplete() {
        UnityAds.load(Value.unityAds.interstitialAd, loadListener)
    }

    override fun onInitializationFailed(p0: UnityAds.UnityAdsInitializationError?, p1: String?) {
        Log.d(TAG, "onInitializationFailed: ")
    }
}