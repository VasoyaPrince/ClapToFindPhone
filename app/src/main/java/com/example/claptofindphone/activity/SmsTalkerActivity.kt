package com.example.claptofindphone.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.ActivitySmstalkerBinding
import com.example.claptofindphone.model.CallList
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.utils.Utils
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.*
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import java.util.*

class SmsTalkerActivity : AppCompatActivity() {
    lateinit var binding: ActivitySmstalkerBinding
    var t1: TextToSpeech? = null
    private var br: BroadcastReceiver? = null
    private val testMode = true
    private lateinit var unityLoadListener: IUnityAdsLoadListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmstalkerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.title_sms_layout)
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
                    // Handle the failure by logging, altering the UI, and so on.
                    //Toast.makeText(this@ClapToFindPhone, "failed", Toast.LENGTH_SHORT).show()
                }
            })
            .build()

        adLoader1.loadAd(AdRequest.Builder().build())

        t1 = TextToSpeech(
            applicationContext
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                t1?.language = Locale.US
            }
        }

        activityResultLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECEIVE_SMS,
            )
        )

        binding.setSmsTalkerMode.setOnCheckedChangeListener { p, isChecked ->
            if (isChecked) {
                UnityAds.show(this, Value.unityAds.interstitialAd, showListener2())
            } else {
                unregisterReceiver(br)
            }
        }


    }

    @SuppressLint("Recycle", "Range")
    private fun getAllContacts(): ArrayList<CallList> {
        val callList: ArrayList<CallList> = ArrayList()
        val cr = contentResolver
        val cur: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )

        if (cur != null) {
            if (cur.count > 0) {
                while (cur.moveToNext()) {
                    val id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID)
                    )
                    val name = cur.getString(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME
                        )
                    )

                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        val pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        while (pCur!!.moveToNext()) {
                            val phoneNo = pCur.getString(
                                pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                )
                            )

                            callList.add(CallList(name, phoneNo))
                        }

                        pCur.close()
                    }
                }
            }
        }

        cur?.close()
        return callList
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.setSmsTalkerMode.isChecked = true
                receiveMsg()
            } else {
                binding.setSmsTalkerMode.isChecked = false
            }
        }
    }

    private fun receiveMsg() {
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val callList: ArrayList<CallList> = ArrayList()
                if (ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    callList.addAll(getAllContacts())

                }

                for (sms in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    val contact = callList.singleOrNull { e ->
                        e.phoneNo.replace(
                            " ",
                            ""
                        ) == sms.originatingAddress
                    }
                    t1!!.speak(
                        "${contact?.name ?: sms.originatingAddress}${sms.displayMessageBody}",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )


                }
            }

        }
        registerReceiver(br, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        Value.SmsTalker.isBackPress++
        if (Utils.isNetworkConnected(this)) {
            if (Value.SmsTalker.isBackPress == 3) {

                UnityAds.show(this@SmsTalkerActivity, Value.unityAds.interstitialAd, showListener())
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

    private fun showListener2(): IUnityAdsShowListener = object : IUnityAdsShowListener {
        override fun onUnityAdsShowFailure(
            placementId: String?,
            error: UnityAds.UnityAdsShowError?,
            message: String?
        ) {
            receiveMsg()
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
            receiveMsg()
            if (UnityAds.isInitialized()) {
                UnityAds.load(Value.unityAds.interstitialAd, unityLoadListener)
            }
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                binding.setSmsTalkerMode.isEnabled = isGranted

            }

        }

}