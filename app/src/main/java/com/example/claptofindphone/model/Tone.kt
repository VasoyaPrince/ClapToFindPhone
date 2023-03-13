package com.example.claptofindphone.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tone")
data class Tone(
    @PrimaryKey
//    @ColumnInfo(name = "toneName")
    val toneName: String,
//    @ColumnInfo(name = "bytes")
    val bytes: ByteArray,
//    @ColumnInfo(name = "isSelected")
    var isSelected: Int = 0,
) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Tone
        if (toneName != other.toneName) return false
        return true
    }

    override fun hashCode(): Int {
        return toneName.hashCode()
    }
}
