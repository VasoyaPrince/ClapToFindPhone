package com.example.claptofindphone.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.ActivityPocketModeBinding
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.service.PocketService
import com.example.claptofindphone.utils.DataStoreRepository
import com.example.claptofindphone.utils.Utils
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.*
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PocketModeActivity : AppCompatActivity(), IUnityAdsInitializationListener {
    lateinit var binding: ActivityPocketModeBinding
    private var cdt: CountDownTimer? = null
    private lateinit var dataStoreRepo: DataStoreRepository
    var isSmartMode: Boolean = false
    private val testMode = true
    private lateinit var unityLoadListener: IUnityAdsLoadListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPocketModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.title_pocket_layout)

        supportActionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayHomeAsUpEnabled(true)
        }

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

        dataStoreRepo = DataStoreRepository(this)

        CoroutineScope(Dispatchers.IO).launch {
            isSmartMode = dataStoreRepo.getPocketModeValue.first()
            Handler(Looper.getMainLooper()).post {
                binding.setSwitchSmartMode.isChecked = isSmartMode
                binding.setSwitchSmartMode.setOnCheckedChangeListener { _, isChecked ->

                    if (isChecked) {
                        if (Utils.isNetworkConnected(this@PocketModeActivity)) {
                            isSmartMode = if (UnityAds.isInitialized()) {
                                UnityAds.show(
                                    this@PocketModeActivity,
                                    Value.unityAds.interstitialAd,
                                    showListener1()
                                )
                                isChecked
                            } else {
                                isChecked
                            }
                        }
                    } else {
                        isSmartMode = isChecked
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        dataStoreRepo.storePocketModeValue(isChecked)
                    }
                }
            }
        }

        binding.setInfoMode.setOnClickListener {
            startActivity(Intent(this, PocketInfoActivity::class.java))
        }
        //smart mode


        binding.setPocketMode.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                UnityAds.show(this, Value.unityAds.interstitialAd, showListener2())
            } else {
                stopService(intent)
            }
        }


    }


    override fun onBackPressed() {
        Value.PocketMode.isBackPress++
        if (Utils.isNetworkConnected(this)) {
            if (Value.PocketMode.isBackPress == 3) {

                UnityAds.show(
                    this@PocketModeActivity,
                    Value.unityAds.interstitialAd,
                    showListener()
                )
                super.onBackPressed()
                Value.PocketMode.isBackPress = 0
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

    private fun showListener1(): IUnityAdsShowListener = object : IUnityAdsShowListener {
        override fun onUnityAdsShowFailure(
            placementId: String?,
            error: UnityAds.UnityAdsShowError?,
            message: String?
        ) {

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
            if (UnityAds.isInitialized()) {
                UnityAds.load(Value.unityAds.interstitialAd, unityLoadListener)
            }
        }
    }


    private fun showListener2(): IUnityAdsShowListener = object : IUnityAdsShowListener {
        override fun onUnityAdsShowFailure(
            placementId: String?,
            error: UnityAds.UnityAdsShowError?,
            message: String?
        ) {
            val intent = Intent(this@PocketModeActivity, PocketService::class.java)

            val alertDialog = AlertDialog.Builder(this@PocketModeActivity).create()
            alertDialog.setTitle("Keep Phone In Your Pocket")
            alertDialog.setMessage("00:10")

            cdt = object : CountDownTimer(10000, 500) {
                override fun onTick(millisUntilFinished: Long) {
                    alertDialog.setMessage("00:" + millisUntilFinished / 1000)
                }

                override fun onFinish() {
                    var isLocked = true
                    if (isSmartMode) {
                        val myKM =
                            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager)
                        isLocked = myKM.isKeyguardLocked
                    }
                    alertDialog.hide()
                    if (isLocked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        } else {
                            startService(intent)
                        }
                        finish()
                    }
                }
            }.start()
            alertDialog.setCancelable(false)
            alertDialog.show()

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
            val intent = Intent(this@PocketModeActivity, PocketService::class.java)

            val alertDialog = AlertDialog.Builder(this@PocketModeActivity).create()
            alertDialog.setTitle("Keep Phone In Your Pocket")
            alertDialog.setMessage("00:10")

            cdt = object : CountDownTimer(10000, 500) {
                override fun onTick(millisUntilFinished: Long) {
                    alertDialog.setMessage("00:" + millisUntilFinished / 1000)
                }

                override fun onFinish() {
                    var isLocked = true
                    if (isSmartMode) {
                        val myKM =
                            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager)
                        isLocked = myKM.isKeyguardLocked
                    }
                    alertDialog.hide()
                    if (isLocked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        } else {
                            startService(intent)
                        }
                        finish()
                    }
                }
            }.start()
            alertDialog.setCancelable(false)
            alertDialog.show()


            if (UnityAds.isInitialized()) {
                UnityAds.load(Value.unityAds.interstitialAd, unityLoadListener)
            }
        }
    }

    override fun onInitializationComplete() {
        UnityAds.load(Value.unityAds.interstitialAd, unityLoadListener)
    }

    override fun onInitializationFailed(p0: UnityAds.UnityAdsInitializationError?, p1: String?) {

    }
}