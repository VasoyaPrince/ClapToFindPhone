package com.example.claptofindphone.utils

import android.media.MediaDataSource
import java.io.IOException

class ByteArrayMediaDataSource(private var data: ByteArray) : MediaDataSource() {

    @Throws(IOException::class)
    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        var size1: Int = size
        if (position >= data.size) {
            return -1
        }
        if (position + size1 > data.size) {
            size1 -= (position.toInt() + size1) - data.size
        }
        System.arraycopy(data, position.toInt(), buffer, offset, size1)
        return size1
    }

    @Throws(IOException::class)
    override fun getSize(): Long {
        return data.size.toLong()
    }

    @Throws(IOException::class)
    override fun close() {

        // Nothing to do here
    }
}