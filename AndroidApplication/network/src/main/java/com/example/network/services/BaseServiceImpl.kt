package com.example.network.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.plugin
import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.File

abstract class BaseServiceImpl {
    protected var token: String?
        get() = TokenProvider.token
        set(value) { TokenProvider.token = value }

    var client = HttpClient(OkHttp) {
        defaultRequest {
            url("http://10.0.2.2:8080/")
            header("Content-Type", "application/json")
            header("Accept", "application/json")
        }
            expectSuccess = true
        install(Logging) {
            logger = Logger.SIMPLE
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    init {
        client.plugin(HttpSend).intercept { request ->
            token?.let { request.headers.append("Authorization", "Bearer $it") }
            execute(request)
        }
    }

    protected suspend inline fun <reified TResult> get(url: String): TResult {
        return client.get(url).body<TResult>()
    }

    protected suspend inline fun <reified TResult, reified TRequest> post(url: String, requestBody: TRequest?): TResult {
        return client.post(url) { setBody(requestBody) }.body<TResult>()
    }


    protected suspend inline fun <reified TResult, reified TRequest> put(url: String, requestBody: TRequest): TResult {
        return client.put(url) { setBody(requestBody) }.body<TResult>()
    }

    protected suspend inline fun <reified TResult> delete(url: String): TResult {
        return client.delete(url).body<TResult>()
    }

    protected fun FormBuilder.appendFile(
        name: String,
        file: File,
        contentType: ContentType = ContentType.Image.JPEG
    ) {
        append(
            key = name,
            value = file.readBytes(),
            headers = Headers.build {
                append(
                    HttpHeaders.ContentDisposition,
                    "form-data; name=\"$name\"; filename=\"${file.name}\""
                )
                append(HttpHeaders.ContentType, contentType.toString())
            }
        )
    }
    protected suspend inline fun <reified TResult> postMultipart(
        url: String,
        noinline formData: FormBuilder.() -> Unit
    ): TResult {
        return client.post(url) {
            setBody(
                MultiPartFormDataContent(
                    parts = formData(formData)
                )
            )
        }.body()
    }


    protected inline fun <T> safeExecute(apiCall: () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(apiCall())
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }

    fun updateToken(jwtToken: String?) {
        token = jwtToken
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






