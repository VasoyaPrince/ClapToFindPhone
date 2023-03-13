package com.example.claptofindphone.model

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.claptofindphone.clapToFindPhone.model.ResponseData
import com.example.claptofindphone.service.ApiService
import com.example.claptofindphone.service.MainRepository
import com.example.claptofindphone.utils.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val mainRepository: MainRepository = MainRepository(ApiService())
    private val _apiStateFlow: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    val apiStateFlow: StateFlow<ApiState> = _apiStateFlow
    private var applicationRef: Application = application
    //val language = MutableLiveData<Language>()

//    fun setLanguage(language: Language) {
//        setLocate(language.value)
//        this.language.value = language
//    }

//    fun loadLocate() {
//        val sharedPreferences =
//            applicationRef.getSharedPreferences("Settings", Activity.MODE_PRIVATE)
//        val language = sharedPreferences.getString("My_Lang", "")
//        language?.let {
//            val index = languages.indexOfFirst { item -> item.value == language }
//            if (index != -1) {
//                languages[index].isSelected = true
//            } else {
//                languages[0].isSelected = true
//            }
//            if (it.isEmpty()) {
//                return
//            } else {
//                setLocate(it)
//            }
//        }
//    }
//
//    private fun setLocate(lang: String) {
//
//        val locale = Locale(lang)
//
//        Locale.setDefault(locale)
//
//        val config = Configuration()
//
//        config.setLocale(locale)
//        config.setLayoutDirection(locale)
//
//        config.locale = locale
//        applicationRef.createConfigurationContext(config)
//
//        val editor = applicationRef.getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
//        editor.putString("My_Lang", lang)
//        editor.apply()
//    }



    fun getPermission() = viewModelScope.launch {
        mainRepository.getPermission()
            .onStart {
                _apiStateFlow.value = ApiState.Loading
            }
            .catch { e ->
                _apiStateFlow.value = ApiState.FailureFeatures(e)
            }.collect { response ->
                _apiStateFlow.value = ApiState.SuccessFeatures(response)
            }
    }

    fun getPostFlow(packageName: String) = viewModelScope.launch {
        mainRepository.getApiIdFlow(packageName)
            .onStart {
                _apiStateFlow.value = ApiState.Loading
            }
            .catch { e ->
                _apiStateFlow.value = ApiState.FailureResponse(e)
            }.collect { response ->
                _apiStateFlow.value = ApiState.SuccessResponse(response)
            }
    }

    // suspend fun getPostFlow(packageName: String): ResponseData? = mainRepository.getApiId(packageName)

    suspend fun getPost(packageName: String): ResponseData? = mainRepository.getApiId(packageName)
}