package com.example.claptofindphone.model

import com.example.claptofindphone.R

data class Language(
    val name: String,
    val value: String,
    val img: Int,
    var isSelected: Boolean
)

val languages = listOf(
    Language("English", "en", R.drawable.unitedkingdom, true),
    Language("हिन्दी", "hi", R.drawable.india, false),
    Language("پاکستان", "en-rPK", R.drawable.pakistan, false),
    Language("Filipino", "", R.drawable.unitedkingdom, false),
    Language("Português", "", R.drawable.unitedkingdom, false),
    Language("Français", "", R.drawable.unitedkingdom, false),
    Language("Bahasa Indonesia", "", R.drawable.unitedkingdom, false)
)