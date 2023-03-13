package com.example.claptofindphone.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.claptofindphone.model.Tone

@Dao
interface ToneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(tone: Tone):Long

    @Update
    fun update(tone: Tone)

    @Query("SELECT * FROM tone WHERE isSelected=${1}")
    fun getSelectedTone(): Tone

    @Query("SELECT COUNT(*) FROM tone")
    fun getAllToneCount(): Int

    @Query("SELECT * FROM tone")
    fun readAllTons(): LiveData<List<Tone>>

    @Query("UPDATE tone SET isSelected = ${0} WHERE isSelected = ${1}")
    fun removeSelectedTone()
}