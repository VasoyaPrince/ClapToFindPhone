package com.example.claptofindphone.database

import androidx.lifecycle.LiveData
import com.example.claptofindphone.database.dao.ToneDao
import com.example.claptofindphone.model.Tone

class TonsRepository(private val tonsDao: ToneDao) {

    val readAllTons: LiveData<List<Tone>> = tonsDao.readAllTons()

    fun addTons(tone: Tone) {
        val item=tonsDao.insert(tone)

    }
    fun updateTone(tone: Tone) {
        tonsDao.update(tone)
    }

    fun removeSelectedTone() {
        tonsDao.removeSelectedTone()
    }

}