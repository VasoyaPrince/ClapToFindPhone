package com.example.claptofindphone.activity

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.ActivityDontTouchPhoneBinding
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.service.TouchService
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

class DontTouchPhoneActivity : AppCompatActivity() {
    lateinit var binding: ActivityDontTouchPhoneBinding
    private var isFlashing: Boolean = true
    private lateinit var dataStoreRepo: DataStoreRepository
    private var isFlashValue = false
    private val testMode = true
    private lateinit var unityLoadListener: IUnityAdsLoadListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDontTouchPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.title_touch_layout)
        supportActionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayHomeAsUpEnabled(true)
        }

        dataStoreRepo = DataStoreRepository(this)


        TouchService.serviceStop.observe(this) { serviceStatus ->
            binding.setSwitchTouch.isChecked = serviceStatus
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        getSystemService(AUDIO_SERVICE) as AudioManager

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

        binding.setSwitchTouch.setOnCheckedChangeListener { _, isChecked ->
            binding.setSwitchFlash.isEnabled = !isChecked
            if (isChecked) {

                UnityAds.show(
                    this@DontTouchPhoneActivity,
                    Value.unityAds.interstitialAd,
                    showListener2()
                )
                // isFlashing = true
            } else {
                isFlashing = false
                val intent = Intent(this@DontTouchPhoneActivity, TouchService::class.java)
                stopService(intent)
                Log.d("TAG", "touch close: ")
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            isFlashValue = dataStoreRepo.getTouchModeFlashValue.first()
            binding.setSwitchFlash.isChecked = isFlashValue
            binding.setSwitchFlash.setOnCheckedChangeListener { _, isChecked ->
                isFlashValue = isChecked
                if (isChecked) {
                    if (UnityAds.isInitialized()) {
                        UnityAds.show(
                            this@DontTouchPhoneActivity,
                            Value.unityAds.interstitialAd,
                            showListener1()
                        )
                    }
                }

                CoroutineScope(Dispatchers.IO).launch {
                    dataStoreRepo.storeTouchModeFlashValue(isFlashValue)
                }
            }
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
            finish()
        }
    }

    private fun showListener1(): IUnityAdsShowListener = object : IUnityAdsShowListener {
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

            val alertDialog: AlertDialog =
                AlertDialog.Builder(this@DontTouchPhoneActivity).create().apply {
                    setTitle("Will Be Activated In 5 Seconds")
                    setMessage("00:5")
                    setCancelable(false)
                }
            Log.d("TAG", "touch switch on: ")

            object : CountDownTimer(5000, 500) {
                override fun onTick(millisUntilFinished: Long) {
                    alertDialog.setMessage("00:" + millisUntilFinished / 1000)
                }

                override fun onFinish() {
                    val intent = Intent(this@DontTouchPhoneActivity, TouchService::class.java)
                    startService(intent)
                    alertDialog.dismiss()
                }
            }.start()
            alertDialog.show()

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
            val alertDialog: AlertDialog =
                AlertDialog.Builder(this@DontTouchPhoneActivity).create().apply {
                    setTitle("Will Be Activated In 5 Seconds")
                    setMessage("00:5")
                    setCancelable(false)
                }
            Log.d("TAG", "touch switch on: ")

            object : CountDownTimer(5000, 500) {
                override fun onTick(millisUntilFinished: Long) {
                    alertDialog.setMessage("00:" + millisUntilFinished / 1000)
                }

                override fun onFinish() {
                    val intent = Intent(this@DontTouchPhoneActivity, TouchService::class.java)
                    startService(intent)
                    alertDialog.dismiss()
                }
            }.start()
            alertDialog.show()

            if (UnityAds.isInitialized()) {
                UnityAds.load(Value.unityAds.interstitialAd, unityLoadListener)
            }

        }
    }

    private fun flashOn() {
        object : Thread() {
            override fun run() {
                var pattern = true
                var isFlash = false
                val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraListId = cameraManager.cameraIdList[0]
                while (isFlashing) {
                    if (pattern) {
                        try {
                            cameraManager.setTorchMode(cameraListId, true)
                            isFlash = pattern
                        } catch (e: Exception) {
                            Log.d("Exception", "flash on : ${e.message} ")
                        }
                    } else {
                        try {
                            cameraManager.setTorchMode(cameraListId, false)
                            isFlash = pattern
                        } catch (e: Exception) {
                            Log.d("Exception", "flash off: ${e.message} ")
                        }
                    }
                    pattern = !pattern
                    try {
                        sleep(50)
                    } catch (e: Exception) {
                        Log.d("Exception", "Thread: ${e.message} ")
                    }

                }
                if (isFlash) {
                    try {
                        cameraManager.setTorchMode(cameraListId, false)
                    } catch (e: Exception) {
                        Log.d("Exception", "flash off: ${e.message} ")
                    }
                }
            }
        }.start()
    }

    override fun onBackPressed() {
        Value.DontTouchPhone.isBackPress++
        if (Utils.isNetworkConnected(this)) {
            if (Value.DontTouchPhone.isBackPress == 3) {

                UnityAds.show(
                    this@DontTouchPhoneActivity,
                    Value.unityAds.interstitialAd,
                    showListener()
                )
                super.onBackPressed()
                Value.DontTouchPhone.isBackPress = 0
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

}