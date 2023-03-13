package com.example.claptofindphone.model

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.claptofindphone.service.ApiService
import com.example.claptofindphone.service.MainRepository

private val mainRepository: MainRepository = MainRepository(ApiService())

class WorkManagers(context: Context, workParams: WorkerParameters) :
    CoroutineWorker(context, workParams) {


    override suspend fun doWork(): Result {
        return try {
            val packageName: String = applicationContext.packageName
            mainRepository.appCount(packageName)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}