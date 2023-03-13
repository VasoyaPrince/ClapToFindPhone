package com.example.claptofindphone.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.claptofindphone.database.dao.ToneDao
import com.example.claptofindphone.model.Tone


@Database(entities = [Tone::class], version = 1, exportSchema = true)
abstract class ToneDatabase : RoomDatabase() {

    abstract fun toneDao(): ToneDao

    companion object {
        @Volatile
        var INSTANCE: ToneDatabase? = null
        fun getDatabase(context: Context): ToneDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToneDatabase::class.java,
                    "tonsDatabase.db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }

    }
}
