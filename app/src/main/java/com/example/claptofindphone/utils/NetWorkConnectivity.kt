package com.example.claptofindphone.utils

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.lifecycle.LiveData

class NetWorkConnectivity(private val ConnectivityManager: ConnectivityManager) :
    LiveData<Boolean>() {

    constructor(application: Application) : this(application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

    private val networkCallBack = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postValue(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(false)
        }
    }

    override fun onActive() {
        super.onActive()
        val builder = NetworkRequest.Builder()
        ConnectivityManager.registerNetworkCallback(builder.build(), networkCallBack)
    }

    override fun onInactive() {
        super.onInactive()
        ConnectivityManager.unregisterNetworkCallback(networkCallBack)
    }
}