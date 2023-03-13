package com.example.claptofindphone.activity


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.claptofindphone.R
import com.example.claptofindphone.adapter.ToneAdapter
import com.example.claptofindphone.database.TonsViewModel
import com.example.claptofindphone.databinding.ActivityTonsBinding
import com.example.claptofindphone.model.Tone
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.utils.ByteArrayMediaDataSource
import com.example.claptofindphone.utils.Utils
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import java.io.InputStream


class TonsActivity : AppCompatActivity(), IUnityAdsInitializationListener {

    private lateinit var binding: ActivityTonsBinding
    private lateinit var tonsViewModel: TonsViewModel
    private var mediaPlayer: MediaPlayer? = null
    private val testMode = true
    private val adUnitId2 = Value.unityAds.interstitialAd
    private lateinit var loadListener: IUnityAdsLoadListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTonsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.tone_layout)

        //Unity Ads
        loadListener = object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(p0: String?) {
                //UnityAds.show(this@TonsActivity, adUnitId1, showListener())
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

        supportActionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayHomeAsUpEnabled(true)
        }

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P){
            binding.btnAddTone.setOnClickListener {
                if (UnityAds.isInitialized()) {
                    UnityAds.show(this, Value.unityAds.rewardedAd, showListener())
                }
            }
        }else{
            binding.btnAddTone.visibility = View.GONE
        }

        tonsViewModel = ViewModelProvider(this@TonsActivity)[TonsViewModel::class.java]
        tonsViewModel.readAllTone.observe(this@TonsActivity) { tones ->
            binding.recyclerView.adapter =
                ToneAdapter(this@TonsActivity, tones, tonsViewModel) { tone ->
                    stopAudio()
                    mediaPlayer = MediaPlayer()
                    mediaPlayer?.apply {
                        setDataSource(ByteArrayMediaDataSource(tone.bytes))
                        prepare()
                        start()
                    }
                }
        }
    }


    private fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.release()
                mediaPlayer = null
            }
        }
    }

    private var getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val value: Uri? = it.data?.data
                value?.let {
                    val info = contentResolver.openFileDescriptor(value, "r")
                    val size = info?.statSize
                    if (size!! >= 500) {

                    }
                    Log.d("size", "$size ")
                    val inputStream: InputStream = contentResolver.openInputStream(value)!!
                    stopAudio()
                    val bytes = Utils.toByteArray(inputStream)
                    tonsViewModel.addToneWithTone(
                        Tone(
                            toneName = getNameFromUri(value),
                            bytes = bytes,
                            isSelected = 1
                        )
                    )
//                    tonsViewModel.removeSelectedTone()
//
//                    tonsViewModel.addTons(
//                        Tone(
//                            toneName = getNameFromUri(value),
//                            bytes = Utils.toByteArray(inputStream),
//                            isSelected = true
//                        )
//                    )
                }
            }
        }


    @SuppressLint("Range")
    fun getNameFromUri(uri: Uri): String {
        var fileName = ""
        val cursor: Cursor? = contentResolver.query(
            uri, arrayOf(
                MediaStore.Images.ImageColumns.DISPLAY_NAME
            ), null, null, null
        )
        if (cursor != null && cursor.moveToFirst()) {
            fileName =
                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))
        }
        cursor?.close()
        return fileName
    }

    override fun onBackPressed() {
        stopAudio()
        Value.ToneBackPress.isBackPress++
        if (Utils.isNetworkConnected(this)) {
            if (Value.ToneBackPress.isBackPress == 3) {
                if (Utils.isNetworkConnected(this@TonsActivity)) {
                    UnityAds.show(this, Value.unityAds.rewardedAd, showInterstitialAdListener())
                }
                Value.ToneBackPress.isBackPress = 0
                super.onBackPressed()
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


    private fun showListener() = object : IUnityAdsShowListener {
        override fun onUnityAdsShowFailure(
            p0: String?,
            p1: UnityAds.UnityAdsShowError?,
            p2: String?
        ) {
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "audio/*"
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 0.5)
            intent = Intent.createChooser(intent, "Choose Audio File")
            getResult.launch(intent)

            if (UnityAds.isInitialized()) {
                UnityAds.load(Value.unityAds.interstitialAd, loadListener)
            }
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
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "audio/*"
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 0.5)
            intent = Intent.createChooser(intent, "Choose Audio File")
            getResult.launch(intent)

            if (UnityAds.isInitialized()) {
                UnityAds.load(Value.unityAds.interstitialAd, loadListener)
            }
        }
    }

    override fun onInitializationComplete() {
        if (UnityAds.isInitialized()) {
            UnityAds.load(Value.unityAds.rewardedAd, loadListener)
        }
    }

    override fun onInitializationFailed(p0: UnityAds.UnityAdsInitializationError?, p1: String?) {
        Log.e("UnityAdsExample", "Unity Ads initialization failed with error: [$p0] $p1")
        Log.e("UnityAdsExample", "Unity Ads initialization failed with error: [$p0] $p1")
    }

    //unity InterstitialAd
    private fun showInterstitialAdListener(): IUnityAdsShowListener =
        object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(
                placementId: String?,
                error: UnityAds.UnityAdsShowError?,
                message: String?
            ) {
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
                if (UnityAds.isInitialized()) {
                    UnityAds.load(Value.unityAds.interstitialAd, loadListener)
                }
            }
        }

}