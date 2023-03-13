package com.example.claptofindphone.service

import com.example.claptofindphone.clapToFindPhone.model.ResponseData
import com.example.claptofindphone.model.FeaturesIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MainRepository(private val apiService: ApiService) {

    fun getPermission(): Flow<FeaturesIcon?> = flow {
        emit(apiService.getPermission())
    }.flowOn(Dispatchers.IO)

    fun getApiIdFlow(packageName: String): Flow<ResponseData?> = flow {
        emit(apiService.getApiId(packageName))
    }.flowOn(Dispatchers.IO)

    suspend fun getApiId(packageName: String): ResponseData? = apiService.getApiId(packageName)

    suspend fun appCount(packageName: String) {
        return apiService.postAppCount(packageName)
    }
}