package com.example.claptofindphone.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.claptofindphone.R
import com.example.claptofindphone.adapter.LanguageAdapter
import com.example.claptofindphone.databinding.ActivityLanguageBinding
import com.example.claptofindphone.model.LocaleHelper
import com.example.claptofindphone.model.MainViewModel
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.model.languages
import com.example.claptofindphone.utils.Utils
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import java.util.*

class LanguageActivity : AppCompatActivity() {
    lateinit var binding: ActivityLanguageBinding
    private lateinit var unityLoadListener: IUnityAdsLoadListener
    private val testMode = true
    private val mainViewModel: MainViewModel by viewModels()
    private var currentLanguage = "en"
    private var currentLang: String? = null
    lateinit var locale: Locale
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.language_title_layout)
        supportActionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayHomeAsUpEnabled(true)
        }
//        val actionBar = supportActionBar
//        actionBar!!.title = resources.getString(R.string.app_name)
//        val title:String=resources.getString(R.string.app_name)

        val adapter = LanguageAdapter(this, languages) {
            //ainViewModel.setLanguage(it)
            context = LocaleHelper().setLocale(this, it.value)!!
            //actionBar!!.title = resources.getString(R.string.app_name)
        }
        binding.recyclerView.adapter = adapter


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


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        Value.SmsTalker.isBackPress++
        if (Utils.isNetworkConnected(this)) {
            if (Value.SmsTalker.isBackPress == 3) {

                UnityAds.show(this@LanguageActivity, Value.unityAds.interstitialAd, showListener())
                super.onBackPressed()
                Value.SmsTalker.isBackPress = 0
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

//    private fun setLocale(localeName: String) {
//        if (localeName != currentLanguage) {
//            locale = Locale(localeName)
//            val res = resources
//            val dm = res.displayMetrics
//            val conf = res.configuration
//            conf.locale = locale
//            res.updateConfiguration(conf, dm)
//            val refresh = Intent(
//                this,
//                MainActivity::class.java
//            )
//            refresh.putExtra(currentLang, localeName)
//            startActivity(refresh)
//        } else {
//            Toast.makeText(
//                this@LanguageActivity, "Language, , already, , selected)!", Toast.LENGTH_SHORT).show();
//        }
//    }

}