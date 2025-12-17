package com.example.network.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal abstract class BaseServiceImpl {
    private val client = HttpClient(OkHttp) {
        defaultRequest {
            url("127.0.0.1/")
            header("Content-Type", "application/json")
            header("Accept", "application/json")
        }

        install(Logging) {
            logger = Logger.SIMPLE
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    protected suspend inline fun <reified TResult> get(url: String): TResult {
        return client.get(url).body<TResult>()
    }

    protected suspend inline fun <reified TResult, reified TRequest> post(url: String, requestBody: TRequest): TResult {
        return client.post(url) { setBody(requestBody) }.body<TResult>()
    }

    protected suspend inline fun <reified TResult, reified TRequest> put(url: String, requestBody: TRequest): TResult {
        return client.put(url) { setBody(requestBody) }.body<TResult>()
    }

    protected suspend inline fun <reified TResult> delete(url: String): TResult {
        return client.delete(url).body<TResult>()
    }

    protected inline fun <T> safeExecute(apiCall: () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(apiCall())
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }
}

sealed interface ApiResult<T> {
    data class Success<T>(val data: T): ApiResult<T>
    data class Error<T>(val exception: Exception): ApiResult<T>

    fun onSuccess(block: (T) -> Unit): ApiResult<T> {
        if (this is Success) block(data)
        return this
    }

    fun onError(block: (Exception) -> Unit): ApiResult<T> {
        if (this is Error) block(exception)
        return this
    }
}