package com.example.claptofindphone.model

import android.os.Bundle

data class Data(
    val isAllowed: Boolean,
    val name: String,
    var displayName: String,
    val category: String,
    var drawable: Int,
    var activityClass: Class<*>?,
    var bundle: Bundle?,
)