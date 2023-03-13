package com.example.claptofindphone

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.claptofindphone.database.ToneDatabase
import com.example.claptofindphone.database.dao.ToneDao
import com.example.claptofindphone.model.MainViewModel
import com.example.claptofindphone.model.Tone
import com.example.claptofindphone.model.Value
import com.example.claptofindphone.model.WorkManagers
import com.example.claptofindphone.utils.Utils
import kotlinx.coroutines.*
import java.lang.reflect.Field


class App : Application() {
    private lateinit var toneDao: ToneDao
    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private lateinit var connectivityManager: ConnectivityManager
    private val mainViewModel = MainViewModel(this)

    private val networkCallBack = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            if (Value.googleAds.appId.isEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val data = mainViewModel.getPost(packageName)
                    if (data != null) {
                        for (i in 0 until data.data.adsProvider.size) {
                            if (data.data.adsProvider[i].name == "Google Adsense") {
                                for (j in 0 until data.data.adsProvider[i].fields.size) {
                                    when (data.data.adsProvider[i].fields[j].name) {
                                        "Application Id" -> {
                                            Value.googleAds.appId =
                                                data.data.adsProvider[i].fields[j].value
                                        }
                                        "Interstitial Ad" -> {
                                            Value.googleAds.interstitialAd =
                                                data.data.adsProvider[i].fields[j].value
                                        }
                                        "Native Ad" -> {
                                            Value.googleAds.nativeAd =
                                                data.data.adsProvider[i].fields[j].value
                                        }
                                        "Rewarded Ad" -> {
                                            Value.googleAds.rewardedAd =
                                                data.data.adsProvider[i].fields[j].value
                                        }
                                    }

                                }
                            } else if (data.data.adsProvider[i].name == "Unity") {
                                for (j in 0 until data.data.adsProvider[i].fields.size) {
                                    when (data.data.adsProvider[i].fields[j].name) {
                                        "Unity Game Id" -> {
                                            Value.unityAds.unityGameId =
                                                data.data.adsProvider[i].fields[j].value
                                        }
                                        "Rewarded Ad" -> {
                                            Value.unityAds.rewardedAd =
                                                data.data.adsProvider[i].fields[j].value
                                        }
                                        "Intertitial Ad" -> {
                                            Value.unityAds.interstitialAd =
                                                data.data.adsProvider[i].fields[j].value
                                        }
                                    }
                                }
                            }
                        }
//                        Handler(Looper.getMainLooper()).post {
//                            Toast.makeText(
//                                this@App,
//                                "connected network",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
                    } else {
                        Log.d(ContentValues.TAG, "error")
                    }
                }
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            //Toast.makeText(this@App, "No Network", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate() {
        super.onCreate()
        //mainViewModel.loadLocate()
        val builder = NetworkRequest.Builder()
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(builder.build(), networkCallBack)

        CoroutineScope(Dispatchers.IO).launch {
            //API Call
            setWorkManager()
        }

        toneDao = ToneDatabase.getDatabase(this).toneDao()
        scope.launch {
            var count: Int
            withContext(Dispatchers.Default) {
                count = toneDao.getAllToneCount()
            }
            if (count == 0) {
                val fields: Array<Field> = R.raw::class.java.fields
                for (item in fields.indices) {
                    val music: ByteArray =
                        Utils.toByteArray(resources.openRawResource(fields[item].getInt(fields[item])))
                    withContext(Dispatchers.Default) {
                        val item=toneDao.insert(
                            Tone(
                                toneName = fields[item].name,
                                bytes = music,
                                isSelected = if (item == 0) 1 else 0
                            )
                        )
                    }
                   Log.d("Raw Asset Resource: ", fields[item].name)
                    val id = fields[item].getInt(fields[item])
                    Log.d("Raw Asset Name: ", "$id")
                }
            }
        }
    }

    override fun onTerminate() {
        connectivityManager.unregisterNetworkCallback(networkCallBack)
        super.onTerminate()
    }

    private fun setWorkManager() {
        val constraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workManagerRequest =
            OneTimeWorkRequest.Builder(WorkManagers::class.java).setConstraints(constraint).build()
        WorkManager.getInstance(this).enqueue(workManagerRequest)
    }
}