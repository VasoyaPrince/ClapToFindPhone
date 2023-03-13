package com.example.claptofindphone.service

import com.example.claptofindphone.clapToFindPhone.model.ResponseData
import com.example.claptofindphone.model.FeaturesIcon
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*

class ApiService {

    private val client = HttpClient(Android) {

        install(DefaultRequest) {
            headers.append("Content-Type", "application/json")
        }
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        engine {
            connectTimeout = 100_000
            socketTimeout = 100_000
        }
    }

    suspend fun getPermission(): FeaturesIcon? {
        return try {
            client.get(
                Url("https://app-management-glory.herokuapp.com/v1/apps/feature/get")
            )
        } catch (e: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${e.response.status.description}")
            null
        } catch (e: ClientRequestException) {
            // 4xx - responses
            println("Error: ${e.response.status.description}")
            null
        } catch (e: ServerResponseException) {
            // 5xx - responses
            println("Error: ${e.response.status.description}")
            null
        } catch (e: Exception) {
            println("Error: ${e.message}")
            null
        }
    }

    suspend fun getApiId(packageName: String): ResponseData? {
        return try {
            client.get(
                Url("https://app-management-glory.herokuapp.com/v1/apps/package/$packageName")
            )
        } catch (e: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${e.response.status.description}")
            null
        } catch (e: ClientRequestException) {
            // 4xx - responses
            println("Error: ${e.response.status.description}")
            null
        } catch (e: ServerResponseException) {
            // 5xx - responses
            println("Error: ${e.response.status.description}")
            null
        } catch (e: Exception) {
            println("Error: ${e.message}")
            null
        }
    }

    suspend fun postAppCount(packageName: String) {
        try {
            client.post(
                Url("https://app-management-glory.herokuapp.com/v1/apps/open")
            ) {
                contentType(ContentType.Application.Json)
                body = mapOf("packageName" to packageName)
            }
        } catch (e: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            // 4xx - responses
            println("Error: ${e.response.status.description}")
        } catch (e: ServerResponseException) {
            // 5xx - responses
            println("Error: ${e.response.status.description}")
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

}