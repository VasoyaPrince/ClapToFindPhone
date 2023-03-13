package com.example.claptofindphone.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.claptofindphone.model.Tone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TonsViewModel(application: Application) : AndroidViewModel(application) {

    val readAllTone: LiveData<List<Tone>>
    private val repository: TonsRepository

    init {
        val toneDao = ToneDatabase.getDatabase(application).toneDao()
        repository = TonsRepository(toneDao)
        readAllTone = repository.readAllTons
    }

    fun addTons(tone: Tone) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTons(tone)
        }
    }

    fun updateTone(tone: Tone) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Default) {
                repository.removeSelectedTone()
            }
            withContext(Dispatchers.Default) {
                repository.updateTone(tone)
            }
        }
    }

    fun addToneWithTone(tone: Tone) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Default) {
                repository.removeSelectedTone()
            }
            withContext(Dispatchers.Default) {
                repository.addTons(tone)
            }
        }
    }

    fun removeSelectedTone() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeSelectedTone()
        }
    }


}