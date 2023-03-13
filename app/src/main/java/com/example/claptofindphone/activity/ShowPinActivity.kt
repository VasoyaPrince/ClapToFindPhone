package com.example.claptofindphone.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.ActivityShowPinBinding
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.utils.DataStoreRepository
import com.example.claptofindphone.utils.Utils
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShowPinActivity : AppCompatActivity() {
    lateinit var binding: ActivityShowPinBinding
    private lateinit var dataStoreRepo: DataStoreRepository
    lateinit var forgetPasswordQue: Array<String>
    lateinit var spinner: String
    private val testMode = true
    private lateinit var unityLoadListener: IUnityAdsLoadListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowPinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.title_pinset_layout)
        supportActionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayHomeAsUpEnabled(true)
        }


        dataStoreRepo = DataStoreRepository(this)

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


        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinner = forgetPasswordQue[position]

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        binding.btnSetPin.setOnClickListener {
            val pin = binding.etPin.text.toString()
            if (pin.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    dataStoreRepo.storePinValue(pin)

                    val questionAns = binding.questionAns.text.toString()

                    if (questionAns.isNotEmpty()) {

                        if (Utils.isNetworkConnected(this@ShowPinActivity)) {
                            displayRewardedAd()
                        }

                        dataStoreRepo.storeAnsValue(questionAns)
                        dataStoreRepo.storeQuestionValue(spinner)
                        val intent = Intent(this@ShowPinActivity, PinActivity::class.java)
                        startActivity(intent)

                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(this@ShowPinActivity, "Fill Ans !", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

            } else {
                Toast.makeText(this, "Fill the password !", Toast.LENGTH_SHORT).show()
            }


        }
        forgetPasswordQue = arrayOf(
            "who are you ?",
            "your favourite colour ?",
            "Your birth date ?"
        )


        val ad = ArrayAdapter(this, android.R.layout.simple_spinner_item, forgetPasswordQue)

        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinner.adapter = ad


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun displayRewardedAd() {
        if (UnityAds.isInitialized()) {
            UnityAds.show(this, Value.unityAds.interstitialAd, showListener())
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
            if (UnityAds.isInitialized()) {
                UnityAds.load(Value.unityAds.interstitialAd, unityLoadListener)
            }
        }
    }
}