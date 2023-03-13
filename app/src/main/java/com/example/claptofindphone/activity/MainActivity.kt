package com.example.claptofindphone.activity


import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.claptofindphone.BuildConfig
import com.example.claptofindphone.R
import com.example.claptofindphone.adapter.FeaturesAdapter
import com.example.claptofindphone.databinding.ActivityMainBinding
import com.example.claptofindphone.model.*
import com.example.claptofindphone.utils.ApiState
import com.example.claptofindphone.utils.DataStoreRepository
import com.example.claptofindphone.utils.Utils
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.*
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var reviewInfo: ReviewInfo
    private lateinit var manager: ReviewManager
    private val adUnitId = Value.unityAds.interstitialAd
    private lateinit var unityLoadListener: IUnityAdsLoadListener
    private var isClapToFindPhone: Boolean = false
    private var isWhistleToFindPhone: Boolean = false
    private var isDontTouchPhone: Boolean = false
    private var isPocketMode: Boolean = false
    private var isChargerMode: Boolean = false
    private var isBatteryAlert: Boolean = false
    private var isChildMode: Boolean = false
    private var isCallerTalker: Boolean = false
    private var isSmsTalker: Boolean = false
    private var isAudio: Boolean = false
    private var isPin: Boolean = false
    lateinit var context: Context
    private lateinit var dataStoreRepo: DataStoreRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        dataStoreRepo = DataStoreRepository(this)

        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val language = sharedPreferences.getString("language", "")
        context = language?.let { LocaleHelper().setLocale(this, "hi") }!!
        //actionBar!!.title = context.resources.getString(R.string.app_name)

        binding.toolbarInclude.share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Clap to find phone")
            var shareMessage = "\nClap to find phone\n\n"
            shareMessage =
                """
                ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
                
                """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "share"))
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

        binding.progress.visibility = View.VISIBLE

        if (Utils.isNetworkConnected(this@MainActivity)) {

            mainViewModel.getPermission()
            lifecycleScope.launchWhenCreated {
                mainViewModel.apiStateFlow.collect {
                    when (it) {

                        is ApiState.SuccessFeatures -> {
                            for (i in 0 until it.FeaturesIcon?.data?.size!!) {
                                when (it.FeaturesIcon.data[i].name) {
                                    "Clap to Find" -> {
                                        isClapToFindPhone = it.FeaturesIcon.data[i].isAllowed
                                        it.FeaturesIcon.data[i].drawable = R.drawable.clapping
                                        it.FeaturesIcon.data[i].activityClass =
                                            ClapToFindPhone::class.java
                                        it.FeaturesIcon.data[i].displayName =
                                            context.resources.getString(R.string.claptofind)
                                        it.FeaturesIcon.data[i].bundle = Bundle().apply {
                                            putInt("type", 0)
                                        }

                                    }
                                    "Whistle to Find" -> {
                                        isWhistleToFindPhone = it.FeaturesIcon.data[i].isAllowed
                                        it.FeaturesIcon.data[i].drawable = R.drawable.whistle
                                        it.FeaturesIcon.data[i].activityClass =
                                            ClapToFindPhone::class.java
                                        it.FeaturesIcon.data[i].displayName =
                                            context.resources.getString(R.string.WhistletoFind)
                                        it.FeaturesIcon.data[i].bundle = Bundle().apply {
                                            putInt("type", 1)
                                        }
                                    }
                                    "Don't Touch" -> {
                                        isDontTouchPhone = it.FeaturesIcon.data[i].isAllowed
                                        it.FeaturesIcon.data[i].drawable = R.drawable.ic_donttouch
                                        it.FeaturesIcon.data[i].displayName =
                                            context.resources.getString(R.string.Donttouch)
                                        it.FeaturesIcon.data[i].activityClass =
                                            DontTouchPhoneActivity::class.java
                                    }
                                    "Pocket Mode" -> {
                                        isPocketMode = it.FeaturesIcon.data[i].isAllowed
                                        it.FeaturesIcon.data[i].drawable = R.drawable.pocket
                                        it.FeaturesIcon.data[i].displayName =
                                            context.resources.getString(R.string.pocketmode)
                                        it.FeaturesIcon.data[i].activityClass =
                                            PocketModeActivity::class.java
                                    }
                                    "Charger Disconnect" -> {
                                        isChargerMode = it.FeaturesIcon.data[i].isAllowed
                                        it.FeaturesIcon.data[i].drawable = R.drawable.disconnect
                                        it.FeaturesIcon.data[i].displayName =
                                            context.resources.getString(R.string.ChargerDisconnect)
                                        it.FeaturesIcon.data[i].activityClass =
                                            ChargerDisconnectActivity::class.java
                                    }
                                    "Battery Alert" -> {
                                        isBatteryAlert = it.FeaturesIcon.data[i].isAllowed
                                        it.FeaturesIcon.data[i].drawable = R.drawable.batteryalert
                                        it.FeaturesIcon.data[i].displayName =
                                            context.resources.getString(R.string.BatteryAlert)
                                        it.FeaturesIcon.data[i].activityClass =
                                            BatteryAlertActivity::class.java
                                    }
                                    "Child Mode" -> {
                                        isChildMode = it.FeaturesIcon.data[i].isAllowed
                                        it.FeaturesIcon.data[i].drawable = R.drawable.ic_child_mode
                                        it.FeaturesIcon.data[i].displayName =
                                            context.resources.getString(R.string.ChildMode)
                                        it.FeaturesIcon.data[i].activityClass =
                                            ChildModeActivity::class.java
                                    }
                                    "Caller Talker" -> {
                                        isCallerTalker = it.FeaturesIcon.data[i].isAllowed
                                        it.FeaturesIcon.data[i].drawable = R.drawable.ic_call
                                        it.FeaturesIcon.data[i].displayName =
                                            context.resources.getString(R.string.CallerTalker)
                                        it.FeaturesIcon.data[i].activityClass =
                                            CallerTalkerActivity::class.java
                                    }
                                    "SMS Talker" -> {
                                        isSmsTalker = it.FeaturesIcon.data[i].isAllowed
                                        it.FeaturesIcon.data[i].drawable = R.drawable.ic_sms
                                        it.FeaturesIcon.data[i].displayName =
                                            context.resources.getString(R.string.SMSTalker)
                                        it.FeaturesIcon.data[i].activityClass =
                                            SmsTalkerActivity::class.java
                                    }
                                    "Audio" -> {
                                        isAudio = it.FeaturesIcon.data[i].isAllowed
                                        it.FeaturesIcon.data[i].drawable = R.drawable.ic_volume
                                        it.FeaturesIcon.data[i].displayName =
                                            context.resources.getString(R.string.Audio)
                                        it.FeaturesIcon.data[i].activityClass =
                                            AudioActivity::class.java
                                    }
                                    "PIN" -> {
                                        isPin = it.FeaturesIcon.data[i].isAllowed
                                        it.FeaturesIcon.data[i].drawable = R.drawable.ic_lock
                                        it.FeaturesIcon.data[i].displayName =
                                            context.resources.getString(R.string.PIN)
                                        it.FeaturesIcon.data[i].activityClass =
                                            PinActivity::class.java
                                    }
                                }

                            }
                            it.FeaturesIcon.data.add(
                                Data(
                                    false,
                                    "Language",
                                    "${R.string.Language}",
                                    "Settings",
                                    R.drawable.language,
                                    LanguageActivity::class.java,
                                    null
                                )
                            )

                            CoroutineScope(Dispatchers.IO).launch {
                                dataStoreRepo.storeFeaturesAllowedValue(
                                    OfflinePermission(
                                        isClapToFindPhone,
                                        isWhistleToFindPhone,
                                        isDontTouchPhone,
                                        isPocketMode,
                                        isChargerMode,
                                        isBatteryAlert,
                                        isChildMode,
                                        isCallerTalker,
                                        isSmsTalker,
                                        isAudio,
                                        isPin,
                                    )
                                )
                            }

                            val featuresMap: Map<String, List<Data>> =
                                it.FeaturesIcon.data.groupBy { data ->
                                    data.category
                                }
                            for (i in featuresMap.keys) {
                                val features = featuresMap[i]
                                val textView = TextView(this@MainActivity).apply {
                                    text = i
                                    textSize = 15f
                                    typeface = Typeface.DEFAULT_BOLD
                                    setTextColor(Color.parseColor("#000000"))
                                    val llp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    llp.setMargins(25, 0, 0, 15)

                                    layoutParams = llp
                                }
                                binding.featureLinearLayout.addView(textView)
                                val recyclerView = RecyclerView(this@MainActivity).apply {

                                    layoutManager = GridLayoutManager(this@MainActivity, 4)
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )

                                }
                                val adapter = FeaturesAdapter(
                                    this@MainActivity,
                                    features!!.filter { permissionValue -> permissionValue.isAllowed })
                                recyclerView.adapter = adapter
                                binding.featureLinearLayout.addView(recyclerView)
                            }
                            //native ads

                            MobileAds.initialize(this@MainActivity)
                            val adLoader1: AdLoader =
                                AdLoader.Builder(this@MainActivity, Value.googleAds.nativeAd)
                                    .forNativeAd { nativeAd ->
                                        val styles =
                                            NativeTemplateStyle.Builder().build()
                                        val template = binding.myTemplate2
                                        template.setStyles(styles)
                                        template.setNativeAd(nativeAd)
                                    }.withAdListener(object : AdListener() {
                                        override fun onAdFailedToLoad(adError: LoadAdError) {
                                            // Handle the failure by logging, altering the UI, and so on.
                                            //Toast.makeText(this@MainActivity, "failed", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                    .build()

                            adLoader1.loadAd(AdRequest.Builder().build())
                            binding.progress.visibility = View.GONE
                        }
                        is ApiState.FailureFeatures -> {
                            Log.d(ContentValues.TAG, "${it.error.message}")

                            //offline app
                            CoroutineScope(Dispatchers.IO).launch {
                                dataStoreRepo.getFeaturesAllowedValue()
                            }

                        }
                        else -> {
                            Log.d("failed", "onCreate: ")
                        }
                    }
                }
            }
        } else {
            //offline app
            CoroutineScope(Dispatchers.IO).launch {

                dataStoreRepo.getFeaturesAllowedValue().collect { it ->

                    OfflinePermission(
                        it.clapToFindPhone,
                        it.WhistleToFindPhone,
                        it.DontTouchPhone,
                        it.PocketMode,
                        it.ChargerMode,
                        it.BatteryAlert,
                        it.ChildMode,
                        it.CallerTalker,
                        it.SmsTalker,
                        it.audio,
                        it.pin
                    ).apply {
                        binding.progress.visibility = View.GONE

                        val data = listOf(
                            Data(
                                it.clapToFindPhone,
                                "Clap to Find",
                                "Clap to Find",
                                "FindMyPhone",
                                R.drawable.clapping,
                                ClapToFindPhone::class.java,
                                Bundle().apply { putInt("type", 0) }),
                            Data(
                                it.WhistleToFindPhone,
                                "Whistle to Find",
                                "Whistle to Find",
                                "FindMyPhone",
                                R.drawable.whistle,
                                ClapToFindPhone::class.java,
                                Bundle().apply { putInt("type", 0) }),
                            Data(
                                it.DontTouchPhone,
                                "Don't Touch",
                                "Don't Touch",
                                "FindMyPhone",
                                R.drawable.ic_donttouch,
                                DontTouchPhoneActivity::class.java,
                                bundle = null
                            ),
                            Data(
                                it.PocketMode,
                                "Pocket Mode",
                                "Pocket Mode",
                                "FindMyPhone",
                                R.drawable.pocket,
                                PocketModeActivity::class.java,
                                bundle = null
                            ),
                            Data(
                                it.ChargerMode,
                                "Charger Disconnect",
                                "Charger Disconnect",
                                "Charger Disconnect & Battery Alert",
                                R.drawable.ic_battery,
                                ChargerDisconnectActivity::class.java,
                                bundle = null
                            ),
                            Data(
                                it.BatteryAlert,
                                "Battery Alert",
                                "Battery Alert",
                                "Charger Disconnect & Battery Alert",
                                R.drawable.batteryalert,
                                BatteryAlertActivity::class.java,
                                bundle = null
                            ),
                            Data(
                                it.ChildMode,
                                "Child Mode",
                                "Child Mode",
                                "Charger Disconnect & Battery Alert",
                                R.drawable.ic_child_mode,
                                ChildModeActivity::class.java,
                                bundle = null
                            ),
                            Data(
                                it.CallerTalker,
                                "Caller Talker",
                                "Caller Talker",
                                "Call & SMS",
                                R.drawable.ic_call,
                                CallerTalkerActivity::class.java,
                                bundle = null
                            ),
                            Data(
                                it.SmsTalker,
                                "SMS Talker",
                                "SMS Talker",
                                "Call & SMS",
                                R.drawable.ic_sms,
                                SmsTalkerActivity::class.java,
                                bundle = null
                            ),
                            Data(
                                it.audio,
                                "Audio",
                                "Audio",
                                "Settings",
                                R.drawable.ic_volume,
                                AudioActivity::class.java,
                                bundle = null
                            ),
                            Data(
                                it.pin,
                                "Pin",
                                "Pin",
                                "Settings",
                                R.drawable.ic_lock,
                                PinActivity::class.java,
                                bundle = null
                            ),
                            Data(
                                false,
                                "Language",
                                "Language",
                                "Settings",
                                R.drawable.language,
                                LanguageActivity::class.java,
                                null
                            ),
                        )


                        val featuresMap: Map<String, List<Data>> =
                            data.groupBy {
                                it.category
                            }
                        Handler(Looper.getMainLooper()).post {
                            for (i in featuresMap.keys) {
                                val features = featuresMap[i]
                                val textView = TextView(this@MainActivity).apply {
                                    text = i
                                    textSize = 15f
                                    typeface = Typeface.DEFAULT_BOLD
                                    setTextColor(Color.parseColor("#000000"))
                                    val llp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    llp.setMargins(25, 0, 0, 15)

                                    layoutParams = llp
                                }
                                binding.featureLinearLayout.addView(textView)
                                val recyclerView = RecyclerView(this@MainActivity).apply {

                                    layoutManager = GridLayoutManager(this@MainActivity, 4)
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )

                                }

                                val adapter = FeaturesAdapter(
                                    this@MainActivity,
                                    features!!.filter { permissionValue -> permissionValue.isAllowed })
                                recyclerView.adapter = adapter
                                binding.featureLinearLayout.addView(recyclerView)

                            }
                        }
                    }
                }

            }

        }
//        mainViewModel.language.observe(this) {
//            actionBar?.title = resources.getString(R.string.app_name)
//
//        }
    }


    override fun onBackPressed() {

        val builder = AlertDialog.Builder(this)

        builder.setMessage("Would you like to rate our app ?")
            .setCancelable(false)
            .setPositiveButton(
                "Yes,I Love it"
            ) { dialogInterFace, p1 ->

                manager = ReviewManagerFactory.create(this)
                val request: Task<ReviewInfo> = manager.requestReviewFlow()

                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reviewInfo = task.result!!
                        val flow = manager.launchReviewFlow(this@MainActivity, reviewInfo)

                        flow.addOnSuccessListener {
                            //Toast.makeText(this, "Listener", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // @ReviewErrorCode val reviewErrorCode = (task.exception ).errorCode
                    }
                }
            }
            .setNegativeButton(
                "No,Sure "
            ) { _, _ ->
                if (Utils.isNetworkConnected(this)) {
                    if (UnityAds.isInitialized()) {
                        UnityAds.show(this@MainActivity, adUnitId, showListener())
                        super.onBackPressed()
                    }
                } else {
                    super.onBackPressed()
                }
            }
        val alertDialog = builder.create()
        alertDialog.show()

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
}