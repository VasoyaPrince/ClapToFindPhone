package com.example.claptofindphone.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class Utils {
    companion object {
        @Throws(IOException::class)
        fun toByteArray(inputStream: InputStream): ByteArray {
            val out = ByteArrayOutputStream()
            var read = 0
            val buffer = ByteArray(1024)
            while (read != -1) {
                read = inputStream.read(buffer)
                if (read != -1) out.write(buffer, 0, read)
            }
            out.close()
            return out.toByteArray()
        }

        fun isNetworkConnected(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return networkCapabilities != null &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
}